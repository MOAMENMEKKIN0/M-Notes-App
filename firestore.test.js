const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validPreferencesPayload(uid, now) {
  return {
    userId: uid,
    lang: "ar",
    themeId: "mint-clean",
    fontId: "cairo",
    fontWeight: 600,
    topBarStyle: "T11",
    alertStyle: "A2",
    bannerStyle: "B1",
    bannerIcon: "star",
    bannerTitle: "M Notes",
    bannerSub: "",
    bannerImg: "default",
    bannerImgSize: "medium",
    bannerPhotoShape: "ring",
    taskImgSize: "medium",
    cardShape: "ring",
    showBanner: true,
    soundNav: true,
    soundEnter: true,
    soundExit: true,
    soundDone: true,
    swipePages: true,
    swipeClose: true,
    hapticEnabled: true,
    activeRingId: "G1",
    autoBackup: true,
    lastBackupLabel: "",
    createdAt: now,
    updatedAt: now,
  };
}

function validTaskPayload(uid, now) {
  return {
    userId: uid,
    title: "تمرين الجيم",
    meta: "11:00 م",
    imgUri: "",
    repeatMode: "CYCLE",
    cycleSteps: ["رجل", "أماميات", "خلفيات"],
    cycleIndex: 0,
    weekDays: [0, 2, 4],
    alertEnabled: true,
    alertTime: "23:00",
    nextDueDate: "2026-11-18",
    lastAlertedDate: "",
    sortOrder: 1731900000000,
    createdAt: now,
    updatedAt: now,
  };
}

function validRequestPayload(uid, now) {
  return {
    userId: uid,
    title: "قائمة التسوق",
    body: "لبن، بيض، قهوة",
    createdAt: now,
    updatedAt: now,
  };
}

function validBackupPayload(uid, now) {
  return {
    userId: uid,
    accountLabel: "alice@example.com",
    clientTime: "2026-11-18 14:00",
    tasksCount: 2,
    requestsCount: 1,
    snapshotJson: JSON.stringify({ version: "1.0", tasks: [], requests: [] }),
    createdAt: now,
    updatedAt: now,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("tasks").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("tasks")
      .doc("t1")
      .set(validTaskPayload(ALICE_UID, now))
  );
});

test("Authenticated user can create, update, list, and delete their own tasks", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const taskRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("tasks")
    .doc("task_1");

  await assertSucceeds(taskRef.set(validTaskPayload(ALICE_UID, created)));
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("tasks").get()
  );

  // Partial update: advance cycle / due date
  await assertSucceeds(
    taskRef.update({
      cycleIndex: 1,
      nextDueDate: "2026-11-19",
      lastAlertedDate: "",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Partial update: photo URI
  await assertSucceeds(
    taskRef.update({
      imgUri: "file:///photo.jpg",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject immutable createdAt modification
  await assertFails(
    taskRef.update({
      createdAt: new Date(Date.now() - 200),
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Reject ghost field & @DocumentId 'id' in payload
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("tasks")
      .doc("task_2")
      .set({
        id: "task_2",
        ...validTaskPayload(ALICE_UID, created),
      })
  );

  // Reject tampered future timestamp
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("tasks")
      .doc("task_3")
      .set(validTaskPayload(ALICE_UID, new Date(Date.now() + 60000)))
  );

  await assertSucceeds(taskRef.delete());
});

test("Cross-user isolation blocks Bob from Alice's preferences, tasks, requests, and backups", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validPreferencesPayload(ALICE_UID, now))
  );
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("tasks")
      .doc("t1")
      .set(validTaskPayload(ALICE_UID, now))
  );
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("requests")
      .doc("r1")
      .set(validRequestPayload(ALICE_UID, now))
  );
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("backups")
      .doc("latest")
      .set(validBackupPayload(ALICE_UID, now))
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("tasks").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("requests").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("backups").doc("latest").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("tasks")
      .doc("t1")
      .update({ imgUri: "hack", updatedAt: new Date(Date.now() - 500) })
  );
});

test("User preferences, requests, and backups enforce strict schema and updates", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 8000);

  const prefRef = aliceDb.collection("users").doc(ALICE_UID);
  await assertSucceeds(prefRef.set(validPreferencesPayload(ALICE_UID, created)));
  await assertSucceeds(
    prefRef.update({
      themeId: "sky-saas",
      lang: "en",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    prefRef.update({
      themeId: "sky-saas",
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  const reqRef = prefRef.collection("requests").doc("req_1");
  await assertSucceeds(reqRef.set(validRequestPayload(ALICE_UID, created)));
  await assertSucceeds(
    reqRef.update({
      title: "Updated Request",
      body: "Updated Body",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  const bkpRef = prefRef.collection("backups").doc("latest");
  await assertSucceeds(bkpRef.set(validBackupPayload(ALICE_UID, created)));
  await assertSucceeds(
    bkpRef.update({
      tasksCount: 5,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});
