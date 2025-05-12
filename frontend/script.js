document.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(window.location.search);
    const quizId = params.get('quizId');
    
    // DOM Elements
    const titleEl = document.getElementById('title');
    const descEl = document.getElementById('description');
    const durationEl = document.getElementById('duration');
    const allowedEl = document.getElementById('allowed');
    const errorEl = document.getElementById('error');
    const successEl = document.getElementById('success');
    const studentIdSelect = document.getElementById('studentId');
    const proceedButton = document.getElementById('proceedButton');
    const studentErrorEl = document.getElementById('studentError');
    
    // Modal Elements
    const accessCodeModal = document.getElementById('accessCodeModal');
    const closeModal = document.querySelector('.close');
    const accessCodeInput = document.getElementById('accessCode');
    const submitAccessCodeBtn = document.getElementById('submitAccessCode');
    const accessCodeErrorEl = document.getElementById('accessCodeError');
    
    // Explicitly ensure modal is hidden when page loads
    accessCodeModal.classList.add('hidden');
    
    // Quiz Interface Elements
    const quizInterface = document.getElementById('quizInterface');
    const timerEl = document.getElementById('timer');
    const submitQuizBtn = document.getElementById('submitQuiz');
    const submitQuizBottomBtn = document.getElementById('submitQuizBottom');
    const allQuestionsContainer = document.getElementById('allQuestions');
    
    // Quiz data
    let allowedStudents = [];
    let quizData = null;
    let sessionData = null;
    let answers = {}; // Store user answers: { questionId: 'A'/'B'/'C'/'D' }
    let timerInterval = null;
    
    if (!quizId) {
      titleEl.textContent = "Quiz ID is missing in URL.";
      return;
    }
    
    try {
      // Get quiz info
      console.log(`Fetching quiz info for quizId: ${quizId}`);
      const quizRes = await fetch(`http://localhost:8080/api/quizzes/${quizId}/info`);
      console.log('Quiz info API response status:', quizRes.status);
      
      if (quizRes.status === 404) {
        console.log('Quiz not found');
        errorEl.textContent = "Quiz not found.";
        errorEl.classList.remove('hidden');
        titleEl.textContent = "Not Found";
        return;
      }
      
      quizData = await quizRes.json();
      console.log('Quiz info data:', quizData);
  
      // Show quiz info
      titleEl.textContent = quizData.title;
      descEl.textContent = quizData.description;
      durationEl.textContent = `Duration: ${quizData.duration} seconds`;
  
      // Get allowed students
      console.log(`Fetching allowed students for quizId: ${quizId}`);
      const allowedRes = await fetch(`http://localhost:8080/api/quizzes/${quizId}/allowed-students`);
      console.log('Allowed students API response status:', allowedRes.status);
      allowedStudents = await allowedRes.json();
      console.log('Allowed students:', allowedStudents);
      
      // Populate student dropdown
      allowedStudents.forEach(studentId => {
          const option = document.createElement('option');
          option.value = studentId;
          option.textContent = studentId;
          studentIdSelect.appendChild(option);
      });
      
      // Display allowed students
      allowedEl.textContent = `Allowed Students: ${allowedStudents.join(', ')}`;
      allowedEl.classList.remove('hidden');
      
      // Handle proceed button click - show access code modal only when clicked
      proceedButton.addEventListener('click', () => {
          const selectedStudentId = studentIdSelect.value;
          
          if (!selectedStudentId) {
              studentErrorEl.textContent = "Please select your Student ID";
              studentErrorEl.classList.remove('hidden');
              return;
          }
          
          // Hide error message if any
          studentErrorEl.classList.add('hidden');
          
          // Show access code modal
          accessCodeModal.classList.remove('hidden');
          accessCodeInput.value = ''; // Clear previous input
          accessCodeErrorEl.classList.add('hidden');
      });
      
      // Close modal on X click
      closeModal.addEventListener('click', () => {
          accessCodeModal.classList.add('hidden');
      });
      
      // Submit access code
      submitAccessCodeBtn.addEventListener('click', async () => {
          const selectedStudentId = studentIdSelect.value;
          const accessCode = accessCodeInput.value.trim();
          
          if (!accessCode) {
              accessCodeErrorEl.textContent = "Please enter an access code";
              accessCodeErrorEl.classList.remove('hidden');
              return;
          }
          
          try {
              console.log('Submitting access code with data:', {
                  quizId,
                  studentId: selectedStudentId,
                  accessCode
              });
              
              const startResponse = await fetch('http://localhost:8080/api/quiz-participation/start-with-code', {
                  method: 'POST',
                  headers: {
                      'Content-Type': 'application/json',
                      'Accept': 'application/json'
                  },
                  body: JSON.stringify({
                      quizId: quizId,
                      studentId: selectedStudentId,
                      accessCode: accessCode
                  })
              });
              
              console.log('Start quiz API response status:', startResponse.status);
              
              if (!startResponse.ok) {
                  // Handle errors based on status
                  errorData = await startResponse.json();
                  console.error('Start quiz API error:', startResponse.status);
                  accessCodeErrorEl.textContent = errorData.message || "Failed to start quiz. Please try again.";
                  accessCodeErrorEl.classList.remove('hidden');
                  return;
              }
              
              // Success, get session data
              sessionData = await startResponse.json();
              console.log('Quiz session data:', sessionData);
              
              // Hide modal and start quiz immediately
              accessCodeModal.classList.add('hidden');
              
              // Hide quiz info and show quiz interface
              document.querySelector('.quiz-info').classList.add('hidden');
              document.querySelector('.quiz-action').classList.add('hidden');
              quizInterface.classList.remove('hidden');
              
              // Initialize quiz interface
              initializeQuiz();
              
          } catch (error) {
              console.error('Error starting quiz:', error);
              accessCodeErrorEl.textContent = "An error occurred. Please try again.";
              accessCodeErrorEl.classList.remove('hidden');
              console.error(error);
          }
      });
      
    } catch (error) {
      console.error('Error loading quiz data:', error);
      errorEl.textContent = "An error occurred while loading the quiz.";
      errorEl.classList.remove('hidden');
      console.error(error);
    }
    
    function initializeQuiz() {
        console.log('Initializing quiz interface');
        // Set up the timer
        startTimer();
        
        // Display all questions
        displayAllQuestions();
        
        // Add event listeners for submit buttons
        submitQuizBtn.addEventListener('click', () => {
            console.log('Submit button clicked (top)');
            if (confirm('Are you sure you want to submit your quiz?')) {
                submitQuiz();
            }
        });
        
        submitQuizBottomBtn.addEventListener('click', () => {
            console.log('Submit button clicked (bottom)');
            if (confirm('Are you sure you want to submit your quiz?')) {
                submitQuiz();
            }
        });
    }
    
    function startTimer() {
        console.log('Starting timer');
        console.log('Session data:', sessionData);
        console.log('End time:', sessionData.endTime);
        
        try {
            // Convert string time to timestamp if needed
            let endTime;
            if (typeof sessionData.endTime === 'string') {
                console.log('Converting string endTime to timestamp');
                endTime = new Date(sessionData.endTime).getTime();
            } else {
                endTime = sessionData.endTime;
            }
            
            console.log('Calculated end time:', endTime);
            
            // Hiển thị lần đầu
            updateTimer(endTime);
            
            // Cập nhật mỗi giây
            timerInterval = setInterval(() => {
                updateTimer(endTime);
            }, 1000);
        } catch (error) {
            console.error('Error in startTimer:', error);
            // Fallback to duration-based timer if there's an error
            const duration = sessionData.quizInfo.duration; // Thời gian làm bài tính bằng giây
            console.log('Using fallback timer with duration:', duration);
            
            const now = new Date().getTime();
            const fallbackEndTime = now + (duration * 1000);
            
            updateTimer(fallbackEndTime);
            timerInterval = setInterval(() => {
                updateTimer(fallbackEndTime);
            }, 1000);
        }
    }
    
    function updateTimer(endTime) {
        const now = Date.now();
        const distance = endTime - now;
        
        console.log(`Timer update: now=${now}, endTime=${endTime}, distance=${distance}`);
        
        if (distance <= 0) {
            console.log("Time's up! Submitting quiz automatically.");
            clearInterval(timerInterval);
            timerEl.textContent = "Time's up!";
            submitQuiz();
            return;
        }
        
        const hours = Math.floor(distance / (1000 * 60 * 60));
        const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((distance % (1000 * 60)) / 1000);
        
        timerEl.textContent = `Time remaining: ${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
    }
    
    function displayAllQuestions() {
        console.log('Displaying all questions:', sessionData.questions);
        allQuestionsContainer.innerHTML = '';
        
        sessionData.questions.forEach((question, index) => {
            // Create question card
            const questionCard = document.createElement('div');
            questionCard.className = 'question-card';
            
            // Question content
            const questionContent = document.createElement('div');
            questionContent.className = 'question-content';
            questionContent.textContent = `${index + 1}. ${question.content}`;
            
            // Question options
            const questionOptions = document.createElement('div');
            questionOptions.className = 'question-options';
            
            const options = [
                { key: 'A', value: question.optionA },
                { key: 'B', value: question.optionB },
                { key: 'C', value: question.optionC },
                { key: 'D', value: question.optionD }
            ];
            
            options.forEach(option => {
                const optionDiv = document.createElement('div');
                optionDiv.className = 'option';
                optionDiv.textContent = `${option.key}. ${option.value}`;
                
                optionDiv.addEventListener('click', () => {
                    // Select this option
                    answers[question.questionId] = option.key;
                    
                    // Update UI
                    questionOptions.querySelectorAll('.option').forEach(el => el.classList.remove('selected'));
                    optionDiv.classList.add('selected');
                });
                
                questionOptions.appendChild(optionDiv);
            });
            
            // Append all to question card
            questionCard.appendChild(questionContent);
            questionCard.appendChild(questionOptions);
            
            // Append to questions container
            allQuestionsContainer.appendChild(questionCard);
        });
    }
    
    function submitQuiz() {
        // Prepare answers array in the required format
        const answersArray = Object.entries(answers).map(([questionId, answer]) => ({
            questionId,
            studentAnswer: answer
        }));
        
        console.log('Submitting quiz with answers:', answersArray);
        
        // Submit answers to backend
        submitAnswers(answersArray);
    }
    
    async function submitAnswers(answersArray) {
        try {
            console.log(`Submitting answers to API with sessionId: ${sessionData.sessionId}`);
            console.log('Request body:', JSON.stringify({
                answers: answersArray
            }));
            
            const submitResponse = await fetch(`http://localhost:8080/api/quiz-participation/${sessionData.sessionId}/submit`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    answers: answersArray
                })
            });
            
            console.log('Submit quiz API response status:', submitResponse.status);
            
            if (submitResponse.ok) {
                const submitData = await submitResponse.json();
                console.log('Quiz submitted successfully');
                // Show success message
                successEl.textContent = submitData.message || "Quiz submitted successfully!";
                successEl.classList.remove('hidden');
                errorEl.classList.add('hidden');
                
                // Disable all options
                const options = document.querySelectorAll('.option');
                options.forEach(option => {
                    option.style.pointerEvents = 'none';
                });
                
                // Disable submit buttons
                submitQuizBtn.disabled = true;
                submitQuizBottomBtn.disabled = true;
                
                // Stop timer
                clearInterval(timerInterval);
            } else {
                // Show error message
                const errorData = await submitResponse.json();
                console.error('Submit quiz API error:', errorData);
                errorEl.textContent = errorData.message || "Failed to submit quiz. Please try again.";
                errorEl.classList.remove('hidden');
                successEl.classList.add('hidden');
            }
        } catch (error) {
            // Show generic error message
            console.error('Error submitting quiz:', error);
            errorEl.textContent = "An error occurred while submitting the quiz.";
            errorEl.classList.remove('hidden');
            successEl.classList.add('hidden');
            console.error(error);
        }
    }
});
