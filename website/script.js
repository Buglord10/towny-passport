const curriculumData = {
  cefr: {
    description: 'Aligned to CEFR bands A1 to C1 with progressive communication goals and grammar complexity.',
    roadmap: [
      { title: 'A1 Foundations', detail: 'Greetings, introductions, numbers, present tense basics, and simple descriptions.' },
      { title: 'A2 Everyday French', detail: 'Daily routine language, near future, object pronouns, and transactional dialogues.' },
      { title: 'B1 Independence', detail: 'Narrating events in past tenses, giving opinions, and handling travel/work contexts.' },
      { title: 'B2 Precision', detail: 'Formal writing, argument building, nuanced connectors, and tense consistency.' },
      { title: 'C1 Mastery', detail: 'Advanced register control, idiomatic command, and complex clause structures.' }
    ],
    quiz: [
      {
        topic: 'A1 Articles',
        question: 'Choose the correct phrase: ___ amie habite à Paris.',
        options: ['Le amie', 'La amie', "L'amie", 'Un amie'],
        answer: "L'amie",
        explainWrong: "Because 'amie' starts with a vowel sound, 'la' contracts to 'l\'' in French."
      },
      {
        topic: 'A2 Near Future',
        question: 'How do you say: We are going to eat?',
        options: ['Nous allons manger', 'Nous mangeons', 'Nous mangions', 'Nous avons mangé'],
        answer: 'Nous allons manger',
        explainWrong: 'The near future uses aller + infinitive: allons + manger.'
      },
      {
        topic: 'B1 Past Tenses',
        question: 'Pick the sentence that correctly uses the imparfait.',
        options: [
          'Hier, j’ai fini mon devoir.',
          'Quand j’étais petit, je jouais au foot.',
          'Demain, je partirai tôt.',
          'Je vais sortir ce soir.'
        ],
        answer: 'Quand j’étais petit, je jouais au foot.',
        explainWrong: 'Imparfait describes habitual past actions and background context.'
      },
      {
        topic: 'B2 Connectors',
        question: 'Select the best connector for contrast: Il est gentil, ___ il est parfois impatient.',
        options: ['donc', 'mais', 'car', 'puisque'],
        answer: 'mais',
        explainWrong: '"Mais" introduces opposition. The other options express cause or consequence.'
      }
    ]
  },
  gcse: {
    description: 'Built around GCSE themes: identity, local area, school, future plans, and social/global issues.',
    roadmap: [
      { title: 'Theme 1: People & Lifestyle', detail: 'Family, relationships, technology, hobbies, and healthy living.' },
      { title: 'Theme 2: Popular Culture', detail: 'Media, films, music, sports, and describing preferences with reasons.' },
      { title: 'Theme 3: Communication & World Around Us', detail: 'Travel, environmental issues, and practical interactions.' },
      { title: 'Theme 4: School & Future', detail: 'School subjects, work experience, ambitions, and plans after school.' },
      { title: 'Exam Skills Layer', detail: 'Translation accuracy, role-play speed, and photo-card spontaneous responses.' }
    ],
    quiz: [
      {
        topic: 'GCSE Theme 1',
        question: 'Translate: I get on well with my brother.',
        options: [
          'Je m’entends bien avec mon frère.',
          'Je suis bien mon frère.',
          'Je m’entend bien avec mon frère.',
          'Je m’entends bien à mon frère.'
        ],
        answer: 'Je m’entends bien avec mon frère.',
        explainWrong: 'The reflexive verb is s’entendre bien avec quelqu’un. Note the final s in m’entends and avec.'
      },
      {
        topic: 'GCSE Theme 2',
        question: 'Choose the best sentence: Last weekend we watched a film.',
        options: [
          'Le week-end dernier, nous regardons un film.',
          'Le week-end dernier, nous avons regardé un film.',
          'Le week-end dernier, nous regarderons un film.',
          'Le week-end dernier, nous regardions un film demain.'
        ],
        answer: 'Le week-end dernier, nous avons regardé un film.',
        explainWrong: 'A completed action last weekend usually takes the passé composé.'
      },
      {
        topic: 'GCSE Theme 3',
        question: 'Which sentence talks about future plans?',
        options: [
          'Je vais visiter la France.',
          'Je visite la France hier.',
          'Je visitais la France.',
          'Je suis visité la France.'
        ],
        answer: 'Je vais visiter la France.',
        explainWrong: 'Near future = aller + infinitive. "Je vais visiter" is the correct future-plans form.'
      }
    ]
  }
};

const trackSelect = document.querySelector('#track');
const trackDescription = document.querySelector('#track-description');
const roadmap = document.querySelector('#roadmap');
const nextQuestionBtn = document.querySelector('#next-question');
const submitBtn = document.querySelector('#submit-answer');
const questionTopic = document.querySelector('#question-topic');
const questionText = document.querySelector('#question-text');
const optionsEl = document.querySelector('#options');
const feedbackEl = document.querySelector('#feedback');

let currentTrack = 'cefr';
let currentQuestion = null;

function renderRoadmap() {
  const data = curriculumData[currentTrack];
  trackDescription.textContent = data.description;
  roadmap.innerHTML = '';

  data.roadmap.forEach((item) => {
    const card = document.createElement('article');
    card.className = 'card';
    card.innerHTML = `<h3>${item.title}</h3><p>${item.detail}</p>`;
    roadmap.appendChild(card);
  });
}

function loadRandomQuestion() {
  const questions = curriculumData[currentTrack].quiz;
  currentQuestion = questions[Math.floor(Math.random() * questions.length)];
  feedbackEl.className = 'feedback';
  feedbackEl.textContent = '';

  questionTopic.textContent = currentQuestion.topic;
  questionText.textContent = currentQuestion.question;
  optionsEl.innerHTML = '';

  currentQuestion.options.forEach((option, index) => {
    const id = `option-${index}`;
    const wrapper = document.createElement('label');
    wrapper.className = 'option';
    wrapper.setAttribute('for', id);
    wrapper.innerHTML = `<input type="radio" id="${id}" name="quiz-option" value="${option}"> ${option}`;
    optionsEl.appendChild(wrapper);
  });
}

function checkAnswer() {
  const selected = document.querySelector('input[name="quiz-option"]:checked');
  if (!selected) {
    feedbackEl.className = 'feedback bad';
    feedbackEl.textContent = 'Please choose an answer first.';
    return;
  }

  const isCorrect = selected.value === currentQuestion.answer;
  if (isCorrect) {
    feedbackEl.className = 'feedback good';
    feedbackEl.textContent = 'Correct! Great job — keep building your fluency.';
  } else {
    feedbackEl.className = 'feedback bad';
    feedbackEl.textContent = `Not quite. Correct answer: ${currentQuestion.answer}. Why: ${currentQuestion.explainWrong}`;
  }
}

trackSelect.addEventListener('change', (event) => {
  currentTrack = event.target.value;
  renderRoadmap();
  loadRandomQuestion();
});

nextQuestionBtn.addEventListener('click', loadRandomQuestion);
submitBtn.addEventListener('click', checkAnswer);

renderRoadmap();
loadRandomQuestion();
