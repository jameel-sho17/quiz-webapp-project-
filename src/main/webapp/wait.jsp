<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
    String ctx = request.getContextPath();
    String pin = (String) session.getAttribute("sessionPin");
    if (pin == null) pin = request.getParameter("pin");
    if (pin == null) pin = "";
    String name = (String) session.getAttribute("playerName");
    if (name == null) name = "";
%>
<!DOCTYPE html>
<html lang="bs">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>QuizMaster-WaitRoom</title>
  <style>
    body { font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial, sans-serif; background:#0f172a; color:#e5e7eb; margin:0; }
    .wrap { max-width: 720px; margin: 40px auto; padding: 24px; position:relative; }
    .card { background:#111827; border:1px solid #1f2937; border-radius:16px; padding: 20px; box-shadow: 0 10px 30px rgba(0,0,0,.35); }
    .title { font-size: 26px; margin: 0 0 10px; font-weight: 700; }
    .muted { color:#9ca3af; }
    .mono { font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace; }
    ul { list-style: none; padding: 0; margin: 0; }
    li { padding: 6px 8px; border-bottom: 1px dashed #1f2937; }
    .count { font-weight:700; }
    .pin { font-size: 38px; letter-spacing: .14em; margin-top: 8px; }
    .top-left { position:absolute; top:16px; left:16px; }
    .ghost { background: transparent; border:1px dashed #374151; color:#e5e7eb; padding: 8px 12px; border-radius:10px; cursor:pointer; }
    /* === Final overlay === */
    .overlay{position:fixed;inset:0;background:rgba(0,0,0,.6);backdrop-filter:saturate(110%) blur(2px);display:none;align-items:center;justify-content:center;z-index:9999;}
    .modal{background:#111827;border:1px solid #1f2937;border-radius:16px;box-shadow:0 20px 60px rgba(0,0,0,.45);width:min(720px,92vw);padding:20px;}
    .modal h2{margin:0 0 10px;font-size:24px}
    .table{width:100%;border-collapse:collapse;margin-top:8px}
    .table th,.table td{border-bottom:1px dashed #1f2937;padding:8px;text-align:left}
    .modal .actions{display:flex;justify-content:flex-end;margin-top:16px}
    .btn{background:#10b981; color:#081016; border:none; padding: 10px 16px; border-radius: 10px; cursor:pointer; font-weight:700; }
  </style>
</head>
<body>
   <form class="top-left" method="post" action="<c:url value='/client/leave'/>">
      <button type="submit" class="ghost">← Back</button>
    </form>
  <div class="wrap">
   

    <div class="card">
      <h1 class="title">Wait for your question</h1>
      <p class="muted">You have successfully connected as <strong><%= name %></strong></p>
      <p class="muted">PIN for this quiz:</p>
      <div class="pin mono"><%= pin %></div>
    </div>

    <div class="card" style="margin-top:14px;">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px;">
        <h2 class="title" style="font-size:20px;margin:0;">List of players</h2>
        <div class="count"><span id="count">0</span> players</div>
      </div>
      <ul id="players"></ul>
      <p class="muted" id="statusLine" style="margin-top:10px;">Status: waiting for a question...</p>
    </div>
  </div>
  <!-- Final overlay -->
<div id="finalOverlay" class="overlay" aria-modal="true" role="dialog">
  <div class="modal">
    <h2 class="title" style="font-size:24px;">Final result</h2>
    <table class="table">
      <thead><tr><th>#</th><th>Player</th><th>Points</th></tr></thead>
      <tbody id="finalTop10"></tbody>
    </table>
    <div class="actions">
      <form method="post" action="<c:url value='/client/leave'/>">
        <button type="submit" class="btn">← Back</button>
      </form>
    </div>
  </div>
</div>

  <script>
    const ctx = "<%= ctx %>";
    const pin = "<%= pin %>";
    const playersEl = document.getElementById("players");
    const countEl = document.getElementById("count");
    const statusLine = document.getElementById("statusLine");

const finalOverlay = document.getElementById("finalOverlay");
const finalTop10   = document.getElementById("finalTop10");
let __finalShown   = false;
let __pollId       = null;

function escapeHtml(s){
  return String(s).replace(/[&<>"']/g, function(c){
    return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]);
  });
}

async function showFinalOverlay(){
  try {
    const r = await fetch(ctx + "/api/session/scoreboard?pin=" + encodeURIComponent(pin), {cache:"no-store"});
    const data = r.ok ? await r.json() : {players:[]};
    finalTop10.innerHTML = "";
    (data.players || []).slice(0,10).forEach(function(p, idx){
      var tr = document.createElement("tr");
      var safeName  = escapeHtml(p && p.name ? p.name : "Nepoznat");
      var safeScore = (p && p.score != null ? p.score : 0);
      tr.innerHTML = "<td>"+(idx+1)+"</td><td>"+safeName+"</td><td>"+safeScore+"</td>";
      finalTop10.appendChild(tr);
    });
  } catch(e){ console.error(e); }
  finalOverlay.style.display = "flex";
  document.body.style.overflow = "hidden"; // spriječi scroll iza overlaya
}
    function keyFor(data){
      const idx = (typeof data.index !== "undefined") ? data.index : (data.question && data.question.id ? data.question.id : "na");
      return `answered_${pin}_${idx}`;
    }

    async function load() { console.debug("[WAIT] poll...");
      try {
        const [sessRes, qRes] = await Promise.all([
          fetch(ctx + "/api/session/status?pin=" + encodeURIComponent(pin), {cache:"no-store"}),
          fetch(ctx + "/api/question/status?pin=" + encodeURIComponent(pin), {cache:"no-store"})
        ]);
        if (!sessRes.ok || !qRes.ok) return;

        const sess = await sessRes.json();
        const q = await qRes.json();

        if (q.status === "CANCELLED") {
          window.location.replace(ctx + "/client/reset?cancelled=1");
          return;
        }

        countEl.textContent = sess.count ?? 0;
playersEl.innerHTML = "";

// sortiraj po bodovima (desc), pa po imenu (asc) ako je isti broj bodova
const score = v => (v != null ? v : 0);
const nameOf = v => (v && v.name ? String(v.name) : "");

const playersSorted = (sess.players || [])
  .slice() // ne diraj original
  .sort((a, b) => {
    const diff = score(b.score) - score(a.score);
    return diff !== 0 ? diff : nameOf(a).localeCompare(nameOf(b));
  });

playersSorted.forEach(p => {
  const li = document.createElement("li");
  li.textContent = (p.name || "Nepoznat") + " — " + score(p.score);
  playersEl.appendChild(li);
});
var isLast = Number.isInteger(q.index) && Number.isInteger(q.total) && (q.index + 1) >= q.total;

        if (q.state === "open") {
          const key = keyFor(q);
          const answeredServer = (q.answered === true);
          //const alreadyLS = localStorage.getItem(key) === "1";
          const already = answeredServer;
          if (q.state === "open" && q.answered === false) {
            window.location.replace(ctx + "/play.jsp");
            console.log("TREBA DA SE DESI REDIRECT")
            return;
          } else {
            statusLine.textContent = "Odgovor predan. Čekanje isteka vremena…";
          }
        } else if (q.state === "closed") {
          if (isLast && q.state === "closed") {
            if (!__finalShown) {
              __finalShown = true;
              showFinalOverlay();
              // zaustavi dalje pollanje (opciono)
            if (__pollId) { clearInterval(__pollId); __pollId = null; }
            }
          }
          // očisti eventualni ključ starog pitanja da iduće pitanje može da se otvori
          try {
            const key = keyFor(q);
            try { localStorage.removeItem(key); } catch(_) {}
            try { localStorage.removeItem(`answered_${pin}_${q.index}`); } catch(_){ }
            try { localStorage.removeItem(`answered_${pin}_${q.question ? q.question.id : "na"}`); } catch(_){ }
          } catch(_) {}
          statusLine.textContent = "Waiting for next question...";
        } else {
          statusLine.textContent = (q.status === "RUNNING") ? "Waiting for next question..." : "Waiting for start...";
        }
      } catch (e) { console.error(e); }
    }

    load();
    __pollId = setInterval(load, 3000);
  </script>
</body>
</html>
