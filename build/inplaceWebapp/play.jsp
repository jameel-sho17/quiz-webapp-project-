<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
    String ctx = request.getContextPath();
    String pin = (String) session.getAttribute("sessionPin");
    if (pin == null) pin = request.getParameter("pin");
    if (pin == null) pin = "";
%>
<!DOCTYPE html>
<html lang="bs">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>QuizMaster-Question</title>
  <style>
    :root{
      --bg:#0b1220;
      --card:#0f172a;
      --muted:#94a3b8;
      --text:#f5f6fa;
      --border:#1f2937;
      --accent:#3b82f6;
    }
    *{box-sizing:border-box}
    body { font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial, sans-serif; background:var(--bg); color:#fff; margin:0; }
    .wrap { max-width: 760px; margin: 40px auto; padding: 24px; position:relative; }
    .card { background:var(--card); border:1px solid var(--border); border-radius:16px; padding: 20px; box-shadow: 0 10px 30px rgba(0,0,0,.35); }
    .title { font-size: 26px; margin: 0 0 10px; font-weight: 700; }
    .muted { color:var(--muted); }
    .answers { display:grid; grid-template-columns:1fr; gap:10px; margin-top:12px; }
    .ans { background:#111827; border:2px solid var(--border); padding:12px; border-radius:12px; cursor:pointer; text-align:left; color: #fff !important;}
    .ans:hover{ border-color:#334155; }
    .ans.selected{ border-color: var(--accent); background:#0b1530; color: #fff !important;}
    .ans.disabled { opacity:.5; cursor:not-allowed; }
    .row{ display:flex; gap:12px; margin-top:12px; align-items:center; }
    .btn{ background:var(--accent); color:#fff; border:none; padding:10px 16px; border-radius:10px; font-weight:700; cursor:pointer; }
    .btn:disabled{ opacity:.6; cursor:not-allowed; }
    .ghost { background: transparent; border:1px dashed var(--border); color:var(--text); padding: 8px 12px; border-radius:10px; cursor:pointer; }
    .top-left { position:absolute; top:16px; left:16px; }
    .info { font-size:14px; color:var(--muted); margin-top:8px; }
  </style>
</head>
<body>
  <form class="top-left" method="post" action="<c:url value='/client/leave'/>">
      <button type="submit" class="ghost">← Back</button>
    </form>
  <div class="wrap">
    

    <div class="card" id="questionCard" style="display:none;">
      <div class="title" id="qText"></div>
      <div class="muted">Time remaining: <span id="timeLeft">--</span>s</div>
      <div class="answers" id="answers"></div>
      <div class="row">
        <button id="finalBtn" class="btn" disabled>Final answer</button>
        <div class="info" id="helper">Please select 1 or more answers.</div>
      </div>
      <div class="info" id="afterSubmit" style="display:none;">Answer submited. Wait for time to run out..</div>
    </div>

    <div class="card" id="waitCard" style="display:none;">
      <div class="title">Čekanje sljedećeg pitanja…</div>
      <form method="get" action="<c:url value='/wait.jsp'/>">
        <button class="btn" type="submit">Idi na ekran čekanja</button>
      </form>
    </div>
  </div>

  <script>
    const ctx = "<%= ctx %>";
    const pin = "<%= pin %>";
    const qTextEl = document.getElementById("qText");
    const timeLeftEl = document.getElementById("timeLeft");
    const answersEl = document.getElementById("answers");
    const finalBtn = document.getElementById("finalBtn");
    const afterSubmit = document.getElementById("afterSubmit");
    const questionCard = document.getElementById("questionCard");
    const waitCard = document.getElementById("waitCard");
    let selected = new Set();
    let locked = false;
    let countdownId = null;
    let currentKey = null;
    let currentQuestionId = null;

    function storageKey(data){
      const idx = (data && typeof data.index !== "undefined") ? data.index : (data && data.question && data.question.id ? data.question.id : "na");
      return `answered_${pin}_${idx}`;
    }

    function gotoWait() { window.location.replace(ctx + "/wait.jsp"); }

    async function boot() {
      try {
        const r = await fetch(ctx + "/api/question/status?pin=" + encodeURIComponent(pin), {cache:"no-store"});
        if (!r.ok) { 
          console.log("gotowait1");
         //gotoWait(); 
          return; }
        const data = await r.json();

        if (data.status === "CANCELLED") { window.location.replace(ctx + "/client/reset?cancelled=1"); return; }
        if (data.status !== "RUNNING") { 
          console.log("gotowait2");
          //gotoWait();
          return; }

        if (data.state === "open" && data.question) {
          try { currentQuestionId = data.question && data.question.id ? data.question.id : currentQuestionId; } catch(_){ }
          currentKey = storageKey(data);
          // pure client-side guard: if already answered for this question index/id, don't show again
          //if (localStorage.getItem(currentKey) === "1") { 
          //  console.log("gotowait3");
            //gotoWait(); 
           //
           // return; }
          renderQuestion(data);
          startLocalCountdown(data.timeLeftSec || 0);
        } else {
          console.log("gotowait4");
          //gotoWait();
        }
      } catch (e) { console.error(e);
        console.log("gotowait5"); 
        //gotoWait(); 

      }
    }

    function renderQuestion(data) {
      questionCard.style.display = "block";
      waitCard.style.display = "none";
      qTextEl.textContent = data.question.text || "";
      renderAnswers(data.question.answers || []);
      finalBtn.disabled = (selected.size === 0) || locked;
    }

    function startLocalCountdown(seconds) {
      let left = Math.max(0, parseInt(seconds,10) || 0);
      timeLeftEl.textContent = left;
      if (countdownId) clearInterval(countdownId);
      countdownId = setInterval(() => {
        left -= 1;
        timeLeftEl.textContent = left;
        if (left <= 0) {
          clearInterval(countdownId);
          countdownId = null;
          // mark as “consumed” if not already answered, to avoid flicker reopen
          try { if (currentKey) localStorage.setItem(currentKey, "1"); } catch(_){}
          gotoWait();
        }
      }, 1000);
    }

    function renderAnswers(arr) {
      selected.clear();
      locked = false;
      afterSubmit.style.display = "none";
      answersEl.innerHTML = "";
      arr.forEach(a => {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "ans";
        btn.dataset.id = a.id;
        btn.textContent = a.text || "";
        btn.onclick = () => {
          if (locked) return;
          if (selected.has(a.id)) selected.delete(a.id); else selected.add(a.id);
          btn.classList.toggle("selected");
          finalBtn.disabled = (selected.size === 0);
        };
        answersEl.appendChild(btn);
      });
    }

    finalBtn.addEventListener("click", async (e) => {
      e.preventDefault();
      if (locked || selected.size === 0) return;
      locked = true;
      finalBtn.disabled = true;
      try {
        const form = new FormData();
        console.debug("[PLAY] selected before submit:", Array.from(selected));
        [...selected].forEach(id => form.append("answerIds", String(id)));
        // DEBUG submit dump
        try { for (const [k,v] of form.entries()) { console.debug("[PLAY] form", k, "=", v); } } catch(e) { console.warn(e); }
        await fetch(ctx + "/api/answer/submit", { method: "POST", body: form });
      } catch (e) { console.error(e); }
      Array.from(document.querySelectorAll(".ans")).forEach(el => { el.classList.add("disabled"); el.disabled = true; });
      afterSubmit.style.display = "block";
      try { if (currentKey) localStorage.setItem(currentKey, "1"); } catch(_){}
      //gotoWait();
    });

    boot();
  </script>
</body>
</html>
