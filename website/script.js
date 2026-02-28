const appData = {
  cefr: {
    description: 'CEFR ladder from A1 to C1 with progressive communication goals.',
    units: [
      { id: 'u1', title: 'Unit 1 · A1 Basics', focus: 'Greetings, introductions, numbers' },
      { id: 'u2', title: 'Unit 2 · A1 Articles & Gender', focus: 'Articles, noun gender, adjective basics' },
      { id: 'u3', title: 'Unit 3 · A2 Daily Routines', focus: 'Present tense, reflexives, frequency adverbs' },
      { id: 'u4', title: 'Unit 4 · B1 Past Narration', focus: 'Passé composé vs imparfait' },
      { id: 'u5', title: 'Unit 5 · B2 Opinions & Connectors', focus: 'Argument, contrast, formal register' }
    ],
    placement: [
      {
        topic: 'A1 Articles',
        question: 'Choose the correct phrase: ___ amie arrive.',
        options: ['La amie', "L'amie", 'Un amie', 'Le amie'],
        answer: "L'amie"
      },
      {
        topic: 'A2 Future Proche',
        question: 'How do you say: I am going to study?',
        options: ['Je vais étudier', 'J’étudie demain hier', 'Je suis étudier', 'Je vais étudié'],
        answer: 'Je vais étudier'
      },
      {
        topic: 'B1 Past Tenses',
        question: 'Which is an imparfait sentence?',
        options: ['Hier, j’ai mangé.', 'Quand j’étais jeune, je lisais beaucoup.', 'Demain, je lirai.', 'Je vais lire.'],
        answer: 'Quand j’étais jeune, je lisais beaucoup.'
      }
    ],
    lessons: [
      {
        unit: 'u1',
        topic: 'Greetings',
        question: 'Translate: Hello, my name is Luc.',
        options: ['Bonjour, je m’appelle Luc.', 'Bonsoir, j’appeler Luc.', 'Salut, je suis appelé Luc.', 'Bonjour, je m’appelles Luc.'],
        answer: 'Bonjour, je m’appelle Luc.',
        explainWrong: 'Use the reflexive form “je m’appelle”. The other options have incorrect verb forms.'
      },
      {
        unit: 'u2',
        topic: 'Gender Agreement',
        question: 'Choose the correct phrase: une maison ___',
        options: ['blanc', 'blanche', 'blanches', 'blancs'],
        answer: 'blanche',
        explainWrong: 'Maison is feminine singular, so the adjective must be feminine singular: blanche.'
      },
      {
        unit: 'u3',
        topic: 'Reflexive Verbs',
        question: 'Translate: We get up at seven.',
        options: ['Nous levons à sept heures.', 'Nous nous levons à sept heures.', 'Nous se levons à sept heures.', 'Nous nous levé à sept heures.'],
        answer: 'Nous nous levons à sept heures.',
        explainWrong: 'Reflexive verbs require pronoun + verb: nous nous levons.'
      },
      {
        unit: 'u4',
        topic: 'Past Tenses',
        question: 'Pick the best sentence for a repeated past habit.',
        options: ['J’ai joué au tennis hier.', 'Je jouais au tennis chaque été.', 'Je vais jouer au tennis.', 'Je jouerai au tennis.'],
        answer: 'Je jouais au tennis chaque été.',
        explainWrong: 'Repeated/habitual actions in the past generally require the imparfait.'
      },
      {
        unit: 'u5',
        topic: 'Connectors',
        question: 'Complete: Je voudrais sortir, ___ je dois réviser.',
        options: ['donc', 'mais', 'parce que', 'alors'],
        answer: 'mais',
        explainWrong: '“Mais” expresses contrast (wanting to go out vs needing to revise).'
      }
    ]
  },
  gcse: {
    description: 'GCSE themes with exam-focused vocabulary, translation and speaking structures.',
    units: [
      { id: 'g1', title: 'Unit 1 · Identity & Family', focus: 'Describing self, family, relationships' },
      { id: 'g2', title: 'Unit 2 · School Life', focus: 'Subjects, opinions, routine, teachers' },
      { id: 'g3', title: 'Unit 3 · Free Time', focus: 'Sports, media, hobbies, reasons' },
      { id: 'g4', title: 'Unit 4 · Travel & Holidays', focus: 'Directions, transport, past holidays' },
      { id: 'g5', title: 'Unit 5 · Future Plans', focus: 'Work, ambition, near/future tense' }
    ],
    placement: [
      {
        topic: 'Theme 1',
        question: 'Translate: I get on well with my sister.',
        options: ['Je m’entends bien avec ma sœur.', 'Je suis bien ma sœur.', 'Je m’entend bien avec ma sœur.', 'Je m’entends bien à ma sœur.'],
        answer: 'Je m’entends bien avec ma sœur.'
      },
      {
        topic: 'Theme 2',
        question: 'Choose the best translation: Last year we visited Paris.',
        options: ['L’année dernière, nous avons visité Paris.', 'L’année dernière, nous visitons Paris.', 'L’année dernière, nous visiterons Paris.', 'L’année dernière, nous visité Paris.'],
        answer: 'L’année dernière, nous avons visité Paris.'
      },
      {
        topic: 'Theme 5',
        question: 'Which sentence is about future plans?',
        options: ['Je vais travailler en ville.', 'Je travaille en ville hier.', 'Je travaillais en ville.', 'Je suis travaillé en ville.'],
        answer: 'Je vais travailler en ville.'
      }
    ],
    lessons: [
      {
        unit: 'g1',
        topic: 'Family',
        question: 'Translate: My brother is funny.',
        options: ['Mon frère est drôle.', 'Ma frère est drôle.', 'Mon frère a drôle.', 'Mon frère est drôles.'],
        answer: 'Mon frère est drôle.',
        explainWrong: 'Frère is masculine singular, so use mon and singular adjective form.'
      },
      {
        unit: 'g2',
        topic: 'School Opinions',
        question: 'Choose the best sentence: I like maths because it is useful.',
        options: ['J’aime les maths parce que c’est utile.', 'J’aime les maths car c’est utile.', 'J’aime les maths parce que il est utile.', 'J’aime maths parce que c’est utile.'],
        answer: 'J’aime les maths parce que c’est utile.',
        explainWrong: 'You need the article “les maths” and natural expression “c’est utile”.'
      },
      {
        unit: 'g3',
        topic: 'Free Time',
        question: 'Translate: We often play football.',
        options: ['Nous jouons souvent au foot.', 'Nous souvent jouons au foot.', 'Nous jouons souvent foot.', 'Nous joue souvent au foot.'],
        answer: 'Nous jouons souvent au foot.',
        explainWrong: 'Correct conjugation is nous jouons and you need “au foot.”'
      },
      {
        unit: 'g4',
        topic: 'Holiday Past',
        question: 'Pick the correct past sentence.',
        options: ['L’été dernier, je vais en Espagne.', 'L’été dernier, je suis allé en Espagne.', 'L’été dernier, j’aller en Espagne.', 'L’été dernier, je vais allé en Espagne.'],
        answer: 'L’été dernier, je suis allé en Espagne.',
        explainWrong: 'Completed past holiday action uses passé composé with être for aller.'
      },
      {
        unit: 'g5',
        topic: 'Future Plans',
        question: 'Translate: I would like to become a doctor.',
        options: ['Je voudrais devenir médecin.', 'Je veux devenir médecin hier.', 'Je voudrais devenu médecin.', 'Je voudrais deviens médecin.'],
        answer: 'Je voudrais devenir médecin.',
        explainWrong: 'After vouloir/voudrais, use an infinitive: devenir.'
      }
    ]
  }
};

const storageKey = 'frenchpath-users-v2';

const trackSelect = document.querySelector('#track');
const trackDescription = document.querySelector('#track-description');
const unitPath = document.querySelector('#unit-path');

const usernameInput = document.querySelector('#username');
const passwordInput = document.querySelector('#password');
const registerBtn = document.querySelector('#register-btn');
const loginBtn = document.querySelector('#login-btn');
const logoutBtn = document.querySelector('#logout-btn');
const authMessage = document.querySelector('#auth-message');
const statsEl = document.querySelector('#stats');

const startPlacementBtn = document.querySelector('#start-placement');
const placementContainer = document.querySelector('#placement-container');
const placementTopic = document.querySelector('#placement-topic');
const placementQuestion = document.querySelector('#placement-question');
const placementOptions = document.querySelector('#placement-options');
const submitPlacementBtn = document.querySelector('#submit-placement');
const placementFeedback = document.querySelector('#placement-feedback');

const questionTopic = document.querySelector('#question-topic');
const questionText = document.querySelector('#question-text');
const optionsEl = document.querySelector('#options');
const feedbackEl = document.querySelector('#feedback');
const submitAnswerBtn = document.querySelector('#submit-answer');
const nextQuestionBtn = document.querySelector('#next-question');

let currentTrack = 'cefr';
let activeUser = null;
let currentLessonQuestion = null;
let placementIndex = 0;
let placementScore = 0;

function loadUsers() {
  return JSON.parse(localStorage.getItem(storageKey) || '{}');
}

function saveUsers(users) {
  localStorage.setItem(storageKey, JSON.stringify(users));
}

function defaultProgress(track) {
  return {
    track,
    xp: 0,
    streak: 0,
    placementLevel: 'Not set',
    completedUnits: []
  };
}

function getUserRecord(username) {
  const users = loadUsers();
  return users[username] || null;
}

function saveUserRecord(username, record) {
  const users = loadUsers();
  users[username] = record;
  saveUsers(users);
}

function setMessage(text, good = false) {
  authMessage.textContent = text;
  authMessage.className = good ? 'good-text' : 'muted';
}

function renderStats() {
  if (!activeUser) {
    statsEl.innerHTML = '<p class="muted">No user logged in.</p>';
    return;
  }

  statsEl.innerHTML = `
    <p><strong>User:</strong> ${activeUser.username}</p>
    <p><strong>Track:</strong> ${activeUser.progress.track.toUpperCase()}</p>
    <p><strong>XP:</strong> ${activeUser.progress.xp}</p>
    <p><strong>Streak:</strong> ${activeUser.progress.streak} day(s)</p>
    <p><strong>Placement:</strong> ${activeUser.progress.placementLevel}</p>
    <p><strong>Completed Units:</strong> ${activeUser.progress.completedUnits.length}</p>
  `;
}

function renderUnits() {
  const units = appData[currentTrack].units;
  unitPath.innerHTML = '';

  units.forEach((unit, index) => {
    const completed = activeUser?.progress.completedUnits.includes(unit.id);
    const unlocked = index === 0 || units[index - 1] && activeUser?.progress.completedUnits.includes(units[index - 1].id);

    const card = document.createElement('article');
    card.className = 'unit-node';
    card.innerHTML = `
      <h3>${unit.title}</h3>
      <p>${unit.focus}</p>
      <p class="unit-state ${completed ? 'done' : unlocked ? 'open' : 'locked'}">
        ${completed ? '✅ Completed' : unlocked ? '🟢 Unlocked' : '🔒 Locked'}
      </p>
      <button type="button" data-unit="${unit.id}" ${unlocked ? '' : 'disabled'}>${completed ? 'Review unit' : 'Start unit'}</button>
    `;

    const button = card.querySelector('button');
    button.addEventListener('click', () => startLessonForUnit(unit.id));
    unitPath.appendChild(card);
  });
}

function randomLessonForUnit(unitId) {
  const matches = appData[currentTrack].lessons.filter((q) => q.unit === unitId);
  return matches[Math.floor(Math.random() * matches.length)] || null;
}

function startLessonForUnit(unitId) {
  const question = randomLessonForUnit(unitId);
  if (!question) {
    feedbackEl.className = 'feedback bad';
    feedbackEl.textContent = 'No lesson loaded for this unit yet.';
    return;
  }

  currentLessonQuestion = question;
  feedbackEl.className = 'feedback';
  feedbackEl.textContent = '';
  questionTopic.textContent = `${question.topic} · ${unitId.toUpperCase()}`;
  questionText.textContent = question.question;
  optionsEl.innerHTML = '';

  question.options.forEach((option, idx) => {
    const id = `lesson-option-${idx}`;
    const wrapper = document.createElement('label');
    wrapper.className = 'option';
    wrapper.innerHTML = `<input id="${id}" type="radio" name="lesson-option" value="${option}"> ${option}`;
    optionsEl.appendChild(wrapper);
  });
}

function checkLessonAnswer() {
  if (!currentLessonQuestion) {
    feedbackEl.className = 'feedback bad';
    feedbackEl.textContent = 'Start a unit first.';
    return;
  }

  const selected = document.querySelector('input[name="lesson-option"]:checked');
  if (!selected) {
    feedbackEl.className = 'feedback bad';
    feedbackEl.textContent = 'Choose an answer first.';
    return;
  }

  if (selected.value === currentLessonQuestion.answer) {
    feedbackEl.className = 'feedback good';
    feedbackEl.textContent = 'Correct! +10 XP';

    if (activeUser) {
      activeUser.progress.xp += 10;
      activeUser.progress.streak += 1;
      if (!activeUser.progress.completedUnits.includes(currentLessonQuestion.unit)) {
        activeUser.progress.completedUnits.push(currentLessonQuestion.unit);
      }
      saveUserRecord(activeUser.username, activeUser);
      renderStats();
      renderUnits();
    }
  } else {
    feedbackEl.className = 'feedback bad';
    feedbackEl.textContent = `Not quite. Correct: ${currentLessonQuestion.answer}. Why: ${currentLessonQuestion.explainWrong}`;
  }
}

function startPlacementQuiz() {
  placementIndex = 0;
  placementScore = 0;
  placementContainer.classList.remove('hidden');
  placementFeedback.className = 'feedback';
  placementFeedback.textContent = '';
  showPlacementQuestion();
}

function showPlacementQuestion() {
  const question = appData[currentTrack].placement[placementIndex];
  if (!question) {
    finishPlacementQuiz();
    return;
  }

  placementTopic.textContent = question.topic;
  placementQuestion.textContent = question.question;
  placementOptions.innerHTML = '';

  question.options.forEach((option, idx) => {
    const id = `placement-option-${idx}`;
    const wrapper = document.createElement('label');
    wrapper.className = 'option';
    wrapper.innerHTML = `<input id="${id}" type="radio" name="placement-option" value="${option}"> ${option}`;
    placementOptions.appendChild(wrapper);
  });
}

function submitPlacement() {
  const question = appData[currentTrack].placement[placementIndex];
  const selected = document.querySelector('input[name="placement-option"]:checked');
  if (!selected) {
    placementFeedback.className = 'feedback bad';
    placementFeedback.textContent = 'Please choose an answer.';
    return;
  }

  if (selected.value === question.answer) {
    placementScore += 1;
  }

  placementIndex += 1;
  showPlacementQuestion();
}

function finishPlacementQuiz() {
  const total = appData[currentTrack].placement.length;
  const ratio = placementScore / total;

  let level = currentTrack === 'gcse' ? 'GCSE Foundation' : 'A1';
  if (ratio > 0.8) {
    level = currentTrack === 'gcse' ? 'GCSE Higher' : 'B1';
  } else if (ratio > 0.5) {
    level = currentTrack === 'gcse' ? 'GCSE Secure Foundation' : 'A2';
  }

  placementFeedback.className = 'feedback good';
  placementFeedback.textContent = `Placement complete: ${placementScore}/${total}. Suggested start: ${level}.`;

  if (activeUser) {
    activeUser.progress.placementLevel = level;
    saveUserRecord(activeUser.username, activeUser);
    renderStats();
  }
}

function register() {
  const username = usernameInput.value.trim();
  const password = passwordInput.value.trim();

  if (!username || !password) {
    setMessage('Enter username and password.');
    return;
  }

  if (getUserRecord(username)) {
    setMessage('User already exists. Try logging in.');
    return;
  }

  const record = {
    username,
    password,
    progress: defaultProgress(currentTrack)
  };

  saveUserRecord(username, record);
  setMessage('Account created. You can now log in.', true);
}

function login() {
  const username = usernameInput.value.trim();
  const password = passwordInput.value.trim();
  const record = getUserRecord(username);

  if (!record || record.password !== password) {
    setMessage('Invalid username/password.');
    return;
  }

  activeUser = record;
  currentTrack = activeUser.progress.track || currentTrack;
  trackSelect.value = currentTrack;

  setMessage(`Logged in as ${username}.`, true);
  renderTrack();
  renderStats();
  renderUnits();
}

function logout() {
  activeUser = null;
  setMessage('Logged out.');
  renderStats();
  renderUnits();
}

function renderTrack() {
  trackDescription.textContent = appData[currentTrack].description;
}

trackSelect.addEventListener('change', (event) => {
  currentTrack = event.target.value;
  renderTrack();

  if (activeUser) {
    activeUser.progress.track = currentTrack;
    saveUserRecord(activeUser.username, activeUser);
    renderStats();
  }

  renderUnits();
  startLessonForUnit(appData[currentTrack].units[0].id);
});

registerBtn.addEventListener('click', register);
loginBtn.addEventListener('click', login);
logoutBtn.addEventListener('click', logout);
startPlacementBtn.addEventListener('click', startPlacementQuiz);
submitPlacementBtn.addEventListener('click', submitPlacement);
submitAnswerBtn.addEventListener('click', checkLessonAnswer);
nextQuestionBtn.addEventListener('click', () => {
  const fallbackUnit = appData[currentTrack].units[0].id;
  startLessonForUnit(currentLessonQuestion?.unit || fallbackUnit);
});

renderTrack();
renderStats();
renderUnits();
startLessonForUnit(appData[currentTrack].units[0].id);
