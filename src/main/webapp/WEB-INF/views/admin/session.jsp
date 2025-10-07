<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
    com.example.quiz.model.QuizSession sessionObj = (com.example.quiz.model.QuizSession) request.getAttribute("session");
    String pin = (sessionObj != null) ? sessionObj.getPin() : request.getParameter("pin");
    if (pin == null) pin = "";
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="bs">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>QuizMaster-QuizSession</title>
  <style>
    body { font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial, sans-serif; background:#0f172a; color:#e5e7eb; margin:0; }
    .wrap { max-width: 980px; margin: 40px auto; padding: 24px; position:relative; }
    .card { background:#111827; border:1px solid #1f2937; border-radius:16px; padding: 20px; box-shadow: 0 10px 30px rgba(0,0,0,.35); }
    .title { font-size: 28px; margin: 0 0 12px; font-weight: 700; }
    .pin { font-size: 64px; letter-spacing: .18em; text-align:center; margin: 8px 0 8px; font-weight: 800; }
    .muted { color:#9ca3af; text-align:center; margin-bottom: 10px; }
    .row { display:flex; gap:12px; justify-content:center; margin-top: 8px; flex-wrap:wrap; }
    button, .btn { background:#10b981; color:#081016; border:none; padding: 10px 16px; border-radius: 10px; cursor:pointer; font-weight:700; }
    button:hover, .btn:hover { filter: brightness(1.05); }
    .ghost { background: transparent; border:1px dashed #374151; color:#e5e7eb; }
    .mono { font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace; }
    .top-left { position:absolute; top: 16px; left: 16px; }
    .disabled { opacity: .6; cursor: not-allowed; }
    ul { list-style:none; padding:0; margin:0; }
    li { padding:6px 8px; border-bottom:1px dashed #1f2937; }
    .count { font-weight:700; margin-top:12px; text-align:right; }
    .grid { display:grid; grid-template-columns: 1.1fr .9fr; gap: 16px; }
    .pill { padding: 6px 10px; border:1px solid #374151; border-radius: 999px; font-size: 14px; }
    /* === Final overlay === */
    .overlay{position:fixed;inset:0;background:rgba(0,0,0,.6);backdrop-filter:saturate(110%) blur(2px);display:none;align-items:center;justify-content:center;z-index:9999;}
    .modal{background:#111827;border:1px solid #1f2937;border-radius:16px;box-shadow:0 20px 60px rgba(0,0,0,.45);width:min(720px,92vw);padding:20px;}
    .modal h2{margin:0 0 10px;font-size:24px}
    .table{width:100%;border-collapse:collapse;margin-top:8px}
    .table th,.table td{border-bottom:1px dashed #1f2937;padding:8px;text-align:left}
    .modal .actions{display:flex;justify-content:flex-end;margin-top:16px}
  </style>
</head>
<body>
  <form class="top-left" method="post" action="<c:url value='/admin/session/cancel'/>">
      <input type="hidden" name="pin" value="<%= pin %>"/>
      <button type="submit" class="btn ghost">← Back</button>
    </form>
  <div class="wrap">
    

    <div class="grid">
      <div class="card">
        <h1 class="title">PIN to enter the quiz</h1>
        <div class="pin mono" id="pin"><%= pin %></div>
        <p class="muted">Share this PIN with your friends.</p>
        <div class="row">
          <button id="copyBtn" type="button">Copy PIN</button>
          <form id="openFirstForm" method="post" action="<c:url value='/admin/session/open-first'/>" style="display:inline">
            <input type="hidden" name="pin" value="<%= pin %>"/>
            <button type="submit" class="btn" id="openBtn">Open first question</button>
          </form>
          <form id="nextForm" method="post" action="<c:url value='/admin/session/next'/>" style="display:none">
            <input type="hidden" name="pin" value="<%= pin %>"/>
            <input type="hidden" name="dur" id="durNext" value="20"/>
            <button type="submit" class="btn" id="nextBtn">Next question</button>
          </form>
        </div>

        <div style="margin-top:16px;">
          <div id="qMeta" class="muted">Question: <span id="qIndex">-</span>/<span id="qTotal">-</span> • Time remaining: <span id="timeLeft">--</span>s</div>
          <h2 id="qText" style="margin:8px 0 0 0;"></h2>
        </div>

        <div style="margin-top:16px;">
          <h3 class="title" style="font-size:22px;">List of players</h3>
          <ul id="players"></ul>
          <div class="count"><span id="count">0</span> players</div>
        </div>
      </div>

      <div class="card">
        <h2 class="title" style="font-size:22px;">Top 10</h2>
        <ul id="top10"></ul>
      </div>
    </div>
  </div>
  <!-- Final overlay -->
<div id="finalOverlay" class="overlay" aria-modal="true" role="dialog">
  <div class="modal">
    <h2 class="title" style="font-size:24px;">Final results</h2>
    <table class="table" id="finalTable">
      <thead><tr><th>#</th><th>Player</th><th>Points</th></tr></thead>
      <tbody id="finalTop10"></tbody>
    </table>
    <div class="actions">
      <form method="post" action="<c:url value='/admin/session/cancel'/>">
        <input type="hidden" name="pin" value="<%= pin %>"/>
        <button type="submit" class="btn">← Back to quizes</button>
      </form>
    </div>
  </div>
</div>

  <script>
    const ctx = "<%= ctx %>";
    const pin = "<%= pin %>";
    const copyBtn = document.getElementById("copyBtn");
    const playersEl = document.getElementById("players");
    const countEl = document.getElementById("count");
    const qIndexEl = document.getElementById("qIndex");
    const qTotalEl = document.getElementById("qTotal");
    const timeLeftEl = document.getElementById("timeLeft");
    const qTextEl = document.getElementById("qText");
    const openFirstForm = document.getElementById("openFirstForm");
    const nextForm = document.getElementById("nextForm");
    const durSel = document.getElementById("durSel");
    const durNext = document.getElementById("durNext");
    const top10El = document.getElementById("top10");
    const finalOverlay = document.getElementById("finalOverlay");
    const finalTop10 = document.getElementById("finalTop10");

function escapeHtml(s){return String(s).replace(/[&<>"']/g,c=>({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));}

async function showFinalOverlay() {
  try {
    const r = await fetch(ctx + "/api/session/scoreboard?pin=" + encodeURIComponent(pin), {cache:"no-store"});
    const data = r.ok ? await r.json() : {players: []};
    finalTop10.innerHTML = "";
    (data.players || []).slice(0,10).forEach((p, idx) => {
      const tr = document.createElement("tr");
      const safeName = escapeHtml(p.name || "Nepoznat");
const safeScore = (p.score != null ? p.score : 0);
tr.innerHTML = "<td>" + (idx + 1) + "</td><td>" + safeName + "</td><td>" + safeScore + "</td>";

      finalTop10.appendChild(tr);
    });
  } catch (e) { console.error(e); }
  finalOverlay.style.display = "flex";
  document.body.style.overflow = "hidden"; // spriječi scroll iza overlaya
}

    // --- sakrij openFirst nakon prvog klika (per-PIN) ---
  (function () {
  const FLAG_KEY = `first_opened_${pin}`;
  // ako je već otvoreno ranije u ovom browseru za ovaj PIN -> sakrij
  if (localStorage.getItem(FLAG_KEY) === "1") {
    openFirstForm.style.display = "none";
  }
  // kad admin klikne "Otvori prvo pitanje" -> zapamti i odmah sakrij
  openFirstForm.addEventListener("submit", function () {
    try { localStorage.setItem(FLAG_KEY, "1"); } catch (_) {}
    openFirstForm.style.display = "none";
  });
})();
  // očisti marker kad admin zatvori/napusti sesiju
  document.querySelector('.top-left')?.addEventListener('submit', () => {
    try { localStorage.removeItem(`first_opened_${pin}`); } catch(_) {}
  });


    copyBtn.addEventListener("click", function(){
      const p = document.getElementById("pin").innerText.trim();
      navigator.clipboard.writeText(p).then(()=>{ this.textContent = "Copied ✓"; });
    });

    async function loadPlayers() {
      try {
        const r = await fetch(ctx + "/api/session/status?pin=" + encodeURIComponent(pin), {cache:"no-store"});
        if (!r.ok) return;
        const data = await r.json();
        countEl.textContent = data.count ?? 0;
        playersEl.innerHTML = "";
        (data.players || []).forEach(p => {
          const li = document.createElement("li");
          li.textContent = p.name || "Unknown";
          playersEl.appendChild(li);
        });
      } catch (e) { console.error(e); }
    }

    async function loadQuestion() {
  try {
    const r = await fetch(ctx + "/api/question/status?pin=" + encodeURIComponent(pin), {cache:"no-store"});
    if (!r.ok) return;
    const data = await r.json();

    qIndexEl.textContent = (data.index ?? 0) + 1;
    qTotalEl.textContent = data.total ?? "";

    // je li posljednje pitanje (index je 0-based)
    const isLast = Number.isInteger(data.index) && Number.isInteger(data.total)
                   && (data.index + 1) >= data.total;

    if (data.state === "open") {
      timeLeftEl.textContent = data.timeLeftSec ?? 0;
      qTextEl.textContent = data.question?.text ?? "";
    } else if (data.state === "closed") {
      timeLeftEl.textContent = 0;
    } else {
      qTextEl.textContent = "";
      timeLeftEl.textContent = "--";
    }

    // kontrola “Sljedeće pitanje”
    if (isLast) {
      if (data.state === "closed") {
        if (!window.__finalShown) {
            window.__finalShown = true;
            // Sakrij "Sljedeće pitanje" ako je vidljivo
            nextForm.style.display = "none";
            showFinalOverlay();
          }
        }
      nextForm.style.display = "none";
    } else {
      // inače – prikazuj samo kada trenutno NIJE open
      if (data.state === "open") {
        nextForm.style.display = "none";
      } else if (data.state === "closed") {
        nextForm.style.display = "inline";
        if (durSel) durNext.value = durSel.value; // guard ako nema durSel-a
      } else {
        nextForm.style.display = "none";
      }
    }

  } catch (e) { console.error(e); }
}

    async function loadTop10() {
      try {
        const r = await fetch(ctx + "/api/session/scoreboard?pin=" + encodeURIComponent(pin), {cache:"no-store"});
        if (!r.ok) return;
        const data = await r.json();
        top10El.innerHTML = "";
        (data.players || []).forEach((p, idx) => {
          const li = document.createElement("li");
          li.textContent = (idx+1)+". " + (p.name || "Nepoznat") + " — " + (p.score ?? 0);
          top10El.appendChild(li);
        });
      } catch (e) { console.error(e); }
    }

    loadPlayers(); loadQuestion(); loadTop10();
    setInterval(loadPlayers, 2000);
    setInterval(loadQuestion, 1000);
    setInterval(loadTop10, 2000);
  </script>
</body>
</html>
