const firebaseConfig = {
  apiKey: "AIzaSyAHIjeuAsazpPq9SV3uWEMeXp2hjDEFwAY",
  authDomain: "m-notes-3a45f.firebaseapp.com",
  projectId: "m-notes-3a45f",
  storageBucket: "m-notes-3a45f.firebasestorage.app",
  messagingSenderId: "881509665525",
  appId: "1:881509665525:web:0733b1ef0a2d569dbc53bc",
  measurementId: "G-LV0FYWNNS7"
};
let cloudReady = false, cloudUser = null, recaptchaVerifier = null, phoneConfirmation = null;
let backupCfg = JSON.parse(localStorage.getItem("mnotes_backup_cfg") || JSON.stringify({
  autoBackup: true, lastBackupTime: "", lastCloudTime: ""
}));
function saveBackupCfg() { localStorage.setItem("mnotes_backup_cfg", JSON.stringify(backupCfg)); }

function initCloudBackup() {
  if (typeof firebase === "undefined" || !firebaseConfig.apiKey || firebaseConfig.apiKey.includes("PUT_YOUR")) {
    cloudReady = false; return;
  }
  try {
    if (!firebase.apps.length) firebase.initializeApp(firebaseConfig);
    cloudReady = true;
    firebase.auth().onAuthStateChanged(user => {
      cloudUser = user;
      if (document.getElementById("backupBody")) renderBackupSection();
    });
  } catch { cloudReady = false; }
}

function collectMNotesSnapshot() {
  return {
    version: "1.0", savedAt: new Date().toISOString(),
    lang: localStorage.getItem("notes_lang") || "ar",
    tasks: JSON.parse(localStorage.getItem("notes_tasks_v3") || "[]"),
    requests: JSON.parse(localStorage.getItem("notes_free_list") || "[]"),
    soundCfg: JSON.parse(localStorage.getItem("notes_sound_cfg") || "{}"),
    gallery3D: JSON.parse(localStorage.getItem("notes_3d_gallery") || "[]"),
    theme: localStorage.getItem("notes_theme") || "mint-clean",
    font: localStorage.getItem("notes_font") || "'Cairo', sans-serif",
    weight: localStorage.getItem("notes_weight") || "600",
    topBarStyle: localStorage.getItem("notes_topbar_style") || "T11",
    alertStyle: localStorage.getItem("notes_alert_style") || "A2",
    bannerStyle: localStorage.getItem("notes_banner_style") || "B1",
    bannerTitle: localStorage.getItem("notes_banner_title") || "M Notes",
    bannerSub: localStorage.getItem("notes_banner_sub") || "",
    cardShape: localStorage.getItem("notes_card_shape") || "ring"
  };
}

function applyMNotesSnapshot(snap) {
  if (!snap) return;
  if (snap.tasks) localStorage.setItem("notes_tasks_v3", JSON.stringify(snap.tasks));
  if (snap.requests) localStorage.setItem("notes_free_list", JSON.stringify(snap.requests));
  if (snap.soundCfg) localStorage.setItem("notes_sound_cfg", JSON.stringify(snap.soundCfg));
  if (snap.gallery3D) localStorage.setItem("notes_3d_gallery", JSON.stringify(snap.gallery3D));
  if (snap.theme) localStorage.setItem("notes_theme", snap.theme);
  if (snap.font) localStorage.setItem("notes_font", snap.font);
  if (snap.weight) localStorage.setItem("notes_weight", snap.weight);
  if (snap.topBarStyle) localStorage.setItem("notes_topbar_style", snap.topBarStyle);
  if (snap.alertStyle) localStorage.setItem("notes_alert_style", snap.alertStyle);
  if (snap.bannerStyle) localStorage.setItem("notes_banner_style", snap.bannerStyle);
  if (snap.bannerTitle) localStorage.setItem("notes_banner_title", snap.bannerTitle);
  if (snap.bannerSub !== undefined) localStorage.setItem("notes_banner_sub", snap.bannerSub);
  if (snap.cardShape) localStorage.setItem("notes_card_shape", snap.cardShape);
  if (snap.lang) localStorage.setItem("notes_lang", snap.lang);
  location.reload();
}

window.loginWithGmail = async function() {
  if (!cloudReady) return alert(lang === "ar" ? "أضف مفاتيح Firebase في backup-cloud.js أولاً" : "Add Firebase config in backup-cloud.js first");
  try {
    const cap = window.Capacitor;
    const nativeAuth = cap && cap.isNativePlatform && cap.isNativePlatform() && cap.Plugins && cap.Plugins.FirebaseAuthentication;
    if (nativeAuth) {
      const res = await nativeAuth.signInWithGoogle();
      const idToken = res && res.credential && res.credential.idToken;
      if (!idToken) throw new Error("Google sign-in failed");
      await firebase.auth().signInWithCredential(firebase.auth.GoogleAuthProvider.credential(idToken));
    } else {
      await firebase.auth().signInWithPopup(new firebase.auth.GoogleAuthProvider());
    }
    renderBackupSection();
  } catch (err) { alert(err.message); }
};

window.sendPhoneOTP = async function() {
  if (!cloudReady) return alert(lang === "ar" ? "أضف مفاتيح Firebase في backup-cloud.js أولاً" : "Add Firebase config in backup-cloud.js first");
  const phone = document.getElementById("backupPhoneInp").value.trim();
  if (!phone.startsWith("+")) return alert(lang === "ar" ? "اكتب الرقم بمفتاح الدولة مثل +2010..." : "Include country code e.g. +2010...");
  try {
    if (!recaptchaVerifier) recaptchaVerifier = new firebase.auth.RecaptchaVerifier("recaptcha-container", { size: "invisible" });
    phoneConfirmation = await firebase.auth().signInWithPhoneNumber(phone, recaptchaVerifier);
    document.getElementById("otpVerifyRow").style.display = "block";
  } catch (err) { alert(err.message); }
};

window.verifyPhoneOTP = async function() {
  const code = document.getElementById("backupOtpInp").value.trim();
  if (!phoneConfirmation || !code) return;
  try { await phoneConfirmation.confirm(code); renderBackupSection(); }
  catch { alert(lang === "ar" ? "الكود غير صحيح" : "Invalid code"); }
};

window.logoutCloud = async function() {
  try {
    const cap = window.Capacitor;
    if (cap && cap.isNativePlatform && cap.isNativePlatform() && cap.Plugins && cap.Plugins.FirebaseAuthentication) await cap.Plugins.FirebaseAuthentication.signOut();
  } catch {}
  if (cloudReady) await firebase.auth().signOut();
  cloudUser = null; renderBackupSection();
};

window.runCloudBackup = async function(isAuto = false) {
  const snap = collectMNotesSnapshot(), timeTxt = new Date().toLocaleString(lang === "ar" ? "ar-EG" : "en-US");
  localStorage.setItem("mnotes_local_latest_backup", JSON.stringify(snap));
  backupCfg.lastBackupTime = timeTxt;
  if (cloudReady && cloudUser) {
    try {
      await firebase.firestore().collection("mnotes_backups").doc(cloudUser.uid).set({
        payload: snap, account: cloudUser.email || cloudUser.phoneNumber || "user",
        clientTime: timeTxt, updatedAt: firebase.firestore.FieldValue.serverTimestamp()
      });
      backupCfg.lastCloudTime = timeTxt;
      if (!isAuto) alert(lang === "ar" ? "تم رفع النسخة أونلاين ✓" : "Cloud backup saved ✓");
    } catch (e) { if (!isAuto) alert(e.message); }
  } else if (!isAuto) {
    alert(lang === "ar" ? "تم حفظ نسخة محلية الآن ✓" : "Local backup saved ✓");
  }
  saveBackupCfg();
  if (document.getElementById("backupBody")) renderBackupSection();
};

window.restoreLatestBackup = async function() {
  if (cloudReady && cloudUser) {
    try {
      const doc = await firebase.firestore().collection("mnotes_backups").doc(cloudUser.uid).get();
      if (doc.exists && doc.data().payload) {
        if (confirm(lang === "ar" ? `استرداد آخر نسخة (${doc.data().clientTime})؟` : `Restore backup (${doc.data().clientTime})?`)) applyMNotesSnapshot(doc.data().payload);
        return;
      }
    } catch (e) { alert(e.message); return; }
  }
  const localSnap = localStorage.getItem("mnotes_local_latest_backup");
  if (localSnap && confirm(lang === "ar" ? "استرداد آخر نسخة محلية محفوظة؟" : "Restore latest local backup?")) applyMNotesSnapshot(JSON.parse(localSnap));
  else if (!localSnap) alert(lang === "ar" ? "لا توجد نسخة محفوظة بعد" : "No backup found yet");
};

window.exportJsonBackup = function() {
  const a = document.createElement("a");
  a.href = URL.createObjectURL(new Blob([JSON.stringify(collectMNotesSnapshot(), null, 2)], { type: "application/json" }));
  a.download = `M-Notes-Backup-${new Date().toISOString().slice(0,10)}.json`; a.click();
};
window.importJsonBackup = function(e) {
  const f = e.target.files[0]; if (!f) return;
  const r = new FileReader(); r.onload = ev => { try { applyMNotesSnapshot(JSON.parse(ev.target.result)); } catch { alert("Invalid File"); } }; r.readAsText(f);
};

window.renderBackupSection = function() {
  const box = document.getElementById("backupBody"); if (!box) return;
  const ar = (typeof lang !== "undefined" ? lang : "ar") === "ar";
  const st = cloudUser
    ? `<span style="color:var(--primary);font-weight:800;">● ${ar ? "متصل:" : "Online:"} ${cloudUser.email || cloudUser.phoneNumber}</span>`
    : `<span style="color:var(--text-muted);">${cloudReady ? (ar ? "غير مسجل الدخول" : "Not signed in") : (ar ? "حفظ محلي (أضف مفاتيح Firebase للأونلاين)" : "Local mode (Add Firebase keys)")}</span>`;
  box.innerHTML = `
    <div id="recaptcha-container"></div>
    <div class="inline-preview-box" style="margin-top:8px;">
      <span class="inline-preview-label">${ar ? "حالة النسخ الاحتياطي:" : "Backup Status:"}</span>
      <div style="font-size:12px;margin-bottom:4px;">${st}</div>
      <div style="font-size:11px;color:var(--text-muted);">${ar ? "آخر نسخة:" : "Last:"} ${backupCfg.lastCloudTime || backupCfg.lastBackupTime || (ar ? "لا يوجد" : "None")}</div>
    </div>
    ${cloudUser ? `<button class="full-soft-btn pressable" style="margin:0 0 10px;border-color:var(--red);color:var(--red);" onclick="logoutCloud()">${ar ? "تسجيل الخروج" : "Sign Out"}</button>` : `
      <button class="full-prim-btn pressable" style="background:#DB4437;margin-bottom:8px;" onclick="loginWithGmail()">${ar ? "تسجيل الدخول بجيميل (Gmail)" : "Sign in with Gmail"}</button>
      <div style="background:var(--bg-main);padding:10px;border-radius:14px;border:1px solid var(--border-soft);margin-bottom:10px;">
        <label class="f-label">${ar ? "أو تسجيل برقم الهاتف (SMS):" : "Or Phone (SMS):"}</label>
        <div style="display:flex;gap:6px;"><input type="tel" class="f-inp" id="backupPhoneInp" placeholder="+2010xxxxxxxx" style="margin:0;direction:ltr;"><button class="close-btn" style="white-space:nowrap;" onclick="sendPhoneOTP()">${ar ? "إرسال" : "Send"}</button></div>
        <div id="otpVerifyRow" style="display:none;margin-top:8px;"><div style="display:flex;gap:6px;"><input type="text" class="f-inp" id="backupOtpInp" placeholder="123456" style="margin:0;direction:ltr;"><button class="close-btn" onclick="verifyPhoneOTP()">${ar ? "تأكيد" : "Verify"}</button></div></div>
      </div>
    `}
    <div class="slim-row ${backupCfg.autoBackup ? 'is-active' : ''}">
      <span class="row-t">${ar ? "نسخ احتياطي تلقائي" : "Auto Backup"}</span>
      <label class="switch"><input type="checkbox" ${backupCfg.autoBackup ? 'checked' : ''} onchange="backupCfg.autoBackup=this.checked;saveBackupCfg();renderBackupSection();"><span class="slider"></span></label>
    </div>
    <button class="full-prim-btn pressable" style="margin-top:8px;" onclick="runCloudBackup(false)">${ar ? "نسخ احتياطي يدوي الآن" : "Manual Backup Now"}</button>
    <button class="full-soft-btn pressable" onclick="restoreLatestBackup()">${ar ? "استرداد آخر نسخة متاحة ↺" : "Restore Latest Backup ↺"}</button>
    <div class="two-cols" style="margin-top:8px;">
      <button class="btn-snooze pressable" onclick="exportJsonBackup()">${ar ? "تصدير ملف" : "Export JSON"}</button>
      <label class="btn-snooze pressable" style="text-align:center;cursor:pointer;">${ar ? "استيراد ملف" : "Import JSON"}<input type="file" accept=".json" onchange="importJsonBackup(event)" style="display:none"></label>
    </div>
  `;
};
window.addEventListener("load", initCloudBackup);
