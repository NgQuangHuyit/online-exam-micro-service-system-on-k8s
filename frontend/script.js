// Import the config file
import config from './config.js';

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
    const studentSearchInput = document.getElementById('studentSearch');
    const studentList = document.getElementById('studentList');
    const selectedStudentIdEl = document.getElementById('selectedStudentId');
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
    let selectedStudentId = '';
    let quizData = null;
    let sessionData = null;
    let answers = {}; // Store user answers: { questionId: 'A'/'B'/'C'/'D' }
    let timerInterval = null;
    
    // Helper function to handle API errors
    const handleApiError = async (response, defaultMessage, errorElement) => {
        try {
            const errorData = await response.json();
            errorElement.textContent = errorData.message || defaultMessage;
            errorElement.classList.remove('hidden');
            return errorData;
        } catch (error) {
            errorElement.textContent = defaultMessage;
            errorElement.classList.remove('hidden');
            console.error('Error parsing error response:', error);
            return { message: defaultMessage };
        }
    };
    
    // Show notification function
    const showNotification = (message, isError = false) => {
        const notification = document.createElement('div');
        notification.className = `notification ${isError ? 'notification-error' : 'notification-success'}`;
        notification.textContent = message;
        
        document.body.appendChild(notification);
        
        // Auto hide after 5 seconds
        setTimeout(() => {
            notification.classList.add('notification-hide');
            setTimeout(() => {
                document.body.removeChild(notification);
            }, 300);
        }, 5000);
    };
    
    // Function to filter and render the student list
    const renderStudentList = (filter = '') => {
        // Clear the current list
        studentList.innerHTML = '';
        
        // Filter students based on search input
        const filteredStudents = filter 
            ? allowedStudents.filter(id => id.toLowerCase().includes(filter.toLowerCase()))
            : allowedStudents;
        
        // Show a message if no results found
        if (filteredStudents.length === 0) {
            const noResultsEl = document.createElement('div');
            noResultsEl.className = 'no-results';
            noResultsEl.textContent = 'No matching student IDs found';
            studentList.appendChild(noResultsEl);
            return;
        }
        
        // Create list items for each student
        filteredStudents.forEach(studentId => {
            const studentItem = document.createElement('div');
            studentItem.className = `student-item ${studentId === selectedStudentId ? 'selected' : ''}`;
            studentItem.textContent = studentId;
            studentItem.dataset.id = studentId;
            
            studentItem.addEventListener('click', () => {
                // Remove selected class from all items
                document.querySelectorAll('.student-item').forEach(item => {
                    item.classList.remove('selected');
                });
                
                // Set this item as selected
                studentItem.classList.add('selected');
                selectedStudentId = studentId;
                selectedStudentIdEl.textContent = selectedStudentId;
                
                // Enable the proceed button
                proceedButton.disabled = false;
                
                // Hide error message if any
                studentErrorEl.classList.add('hidden');
            });
            
            studentList.appendChild(studentItem);
        });
    };
    
    if (!quizId) {
      titleEl.textContent = "Quiz ID is missing in URL.";
      return;
    }
    
    try {
      // Get quiz info
      console.log(`Fetching quiz info for quizId: ${quizId}`);
      const quizRes = await fetch(`${config.apiBaseUrl}/quizzes/${quizId}/info`);
      console.log('Quiz info API response status:', quizRes.status);
      
      if (quizRes.status === 404) {
        console.log('Quiz not found');
        await handleApiError(quizRes, "Quiz not found.", errorEl);
        titleEl.textContent = "Not Found";
        return;
      }
      
      if (!quizRes.ok) {
        await handleApiError(quizRes, "Failed to load quiz information.", errorEl);
        return;
      }
      
      quizData = await quizRes.json();
      console.log('Quiz info data:', quizData);
  
      // Show quiz info
      titleEl.textContent = quizData.title;
      descEl.textContent = quizData.description;
      durationEl.textContent = `Duration: ${quizData.duration} minutes`;
  
      // Get allowed students
      console.log(`Fetching allowed students for quizId: ${quizId}`);
      const allowedRes = await fetch(`${config.apiBaseUrl}/quizzes/${quizId}/allowed-students`);
      console.log('Allowed students API response status:', allowedRes.status);
      
      if (!allowedRes.ok) {
        await handleApiError(allowedRes, "Failed to load allowed students.", errorEl);
        return;
      }
      
      allowedStudents = await allowedRes.json();
      console.log('Allowed students:', allowedStudents);
      
      // Initialize the student list
      renderStudentList();
      
      // Set up search functionality
      studentSearchInput.addEventListener('input', (e) => {
          renderStudentList(e.target.value);
      });
      
      // Display allowed students count
      allowedEl.innerHTML = `<i class="fas fa-users"></i><span>Allowed Students: ${allowedStudents.length} student(s)</span>`;
      allowedEl.classList.remove('hidden');
      
      // Handle proceed button click - show access code modal only when clicked
      proceedButton.addEventListener('click', () => {
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
          
          // Focus on input field for better UX
          accessCodeInput.focus();
      });
      
      // Close modal on X click
      closeModal.addEventListener('click', () => {
          accessCodeModal.classList.add('hidden');
      });
      
      // Also close modal when clicking outside of it
      accessCodeModal.addEventListener('click', (e) => {
          if (e.target === accessCodeModal) {
              accessCodeModal.classList.add('hidden');
          }
      });
      
      // Handle Enter key press for access code input
      accessCodeInput.addEventListener('keypress', (event) => {
          if (event.key === 'Enter') {
              submitAccessCodeBtn.click();
          }
      });
      
      // Submit access code
      submitAccessCodeBtn.addEventListener('click', async () => {
          const accessCode = accessCodeInput.value.trim();
          
          if (!accessCode) {
              accessCodeErrorEl.textContent = "Please enter an access code";
              accessCodeErrorEl.classList.remove('hidden');
              return;
          }
          
          // Show loading state
          submitAccessCodeBtn.disabled = true;
          submitAccessCodeBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Verifying...';
          
          try {
              console.log('Submitting access code with data:', {
                  quizId,
                  studentId: selectedStudentId,
                  accessCode
              });
              
              const startResponse = await fetch(`${config.apiBaseUrl}/quiz-participation/start-with-code`, {
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
                  const errorData = await handleApiError(startResponse, "Failed to start quiz. Please try again.", accessCodeErrorEl);
                  
                  // Reset button state
                  submitAccessCodeBtn.disabled = false;
                  submitAccessCodeBtn.innerHTML = '<i class="fas fa-check"></i> Submit';
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
              
              // Show success notification
              showNotification("Quiz started successfully!");
              
              // Initialize quiz interface
              initializeQuiz();
              
          } catch (error) {
              console.error('Error starting quiz:', error);
              accessCodeErrorEl.textContent = "An error occurred. Please try again.";
              accessCodeErrorEl.classList.remove('hidden');
              console.error(error);
              
              // Reset button state
              submitAccessCodeBtn.disabled = false;
              submitAccessCodeBtn.innerHTML = '<i class="fas fa-check"></i> Submit';
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
            
            // Show notification for time up
            showNotification("Time's up! Submitting your quiz automatically.", true);
            
            submitQuiz();
            return;
        }
        
        const hours = Math.floor(distance / (1000 * 60 * 60));
        const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((distance % (1000 * 60)) / 1000);
        
        timerEl.textContent = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
        
        // Change timer color when time is running low (less than 1 minute)
        if (distance < 60000) {
            timerEl.parentElement.classList.add('timer-warning');
        }
    }
    
    function displayAllQuestions() {
        console.log('Displaying all questions:', sessionData.questions);
        allQuestionsContainer.innerHTML = '';
        
        // Create question navigation
        const navContainer = document.createElement('div');
        navContainer.className = 'question-nav';
        
        // Initialize progress tracking
        const progressText = document.getElementById('progressText');
        const progressFill = document.getElementById('progressFill');
        progressText.textContent = `0/${sessionData.questions.length}`;
        progressFill.style.width = '0%';
        
        // Function to update progress
        const updateProgress = () => {
            const answeredCount = Object.keys(answers).length;
            const totalQuestions = sessionData.questions.length;
            const percentage = (answeredCount / totalQuestions) * 100;
            
            progressText.textContent = `${answeredCount}/${totalQuestions}`;
            progressFill.style.width = `${percentage}%`;
        };
        
        sessionData.questions.forEach((question, index) => {
            // Create question card
            const questionCard = document.createElement('div');
            questionCard.className = 'question-card';
            questionCard.id = `question-${question.questionId}`;
            
            // Question header
            const questionHeader = document.createElement('div');
            questionHeader.className = 'question-header';
            
            // Question number
            const questionNumber = document.createElement('div');
            questionNumber.className = 'question-number';
            questionNumber.textContent = `Question ${index + 1}`;
            
            // Add question header
            questionHeader.appendChild(questionNumber);
            questionCard.appendChild(questionHeader);
            
            // Question content
            const questionContent = document.createElement('div');
            questionContent.className = 'question-content';
            questionContent.textContent = question.content;
            
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
                
                const optionRadio = document.createElement('input');
                optionRadio.type = 'radio';
                optionRadio.name = `question-${question.questionId}`;
                optionRadio.id = `question-${question.questionId}-${option.key}`;
                optionRadio.value = option.key;
                
                const optionLabel = document.createElement('label');
                optionLabel.htmlFor = `question-${question.questionId}-${option.key}`;
                optionLabel.textContent = `${option.key}. ${option.value}`;
                
                optionDiv.appendChild(optionRadio);
                optionDiv.appendChild(optionLabel);
                
                optionRadio.addEventListener('change', () => {
                    // Select this option
                    answers[question.questionId] = option.key;
                    
                    // Update navigation to show answered
                    const navButton = document.getElementById(`nav-${question.questionId}`);
                    if (navButton) {
                        navButton.classList.add('answered');
                    }
                    
                    // Update progress bar
                    updateProgress();
                });
                
                questionOptions.appendChild(optionDiv);
            });
            
            // Append all to question card
            questionCard.appendChild(questionContent);
            questionCard.appendChild(questionOptions);
            
            // Append to questions container
            allQuestionsContainer.appendChild(questionCard);
            
            // Create navigation button for this question
            const navButton = document.createElement('button');
            navButton.className = 'nav-button';
            navButton.id = `nav-${question.questionId}`;
            navButton.textContent = index + 1;
            navButton.addEventListener('click', () => {
                // Scroll to the question
                const questionElement = document.getElementById(`question-${question.questionId}`);
                questionElement.scrollIntoView({ behavior: 'smooth' });
            });
            
            navContainer.appendChild(navButton);
        });
        
        // Add navigation to the beginning
        const navWrapper = document.createElement('div');
        navWrapper.className = 'nav-wrapper';
        navWrapper.appendChild(navContainer);
        
        allQuestionsContainer.insertBefore(navWrapper, allQuestionsContainer.firstChild);
    }
    
    function submitQuiz() {
        // Prepare answers array in the required format
        const answersArray = Object.entries(answers).map(([questionId, answer]) => ({
            questionId,
            studentAnswer: answer
        }));
        
        console.log('Submitting quiz with answers:', answersArray);
        
        // Check if all questions are answered
        if (answersArray.length < sessionData.questions.length) {
            const missingCount = sessionData.questions.length - answersArray.length;
            if (!confirm(`You haven't answered ${missingCount} question(s). Are you sure you want to submit?`)) {
                return;
            }
        }
        
        // Show loading state
        submitQuizBtn.disabled = true;
        submitQuizBottomBtn.disabled = true;
        submitQuizBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Submitting...';
        submitQuizBottomBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Submitting...';
        
        // Submit answers to backend
        submitAnswers(answersArray);
    }
    
    async function submitAnswers(answersArray) {
        try {
            console.log(`Submitting answers to API with sessionId: ${sessionData.sessionId}`);
            console.log('Request body:', JSON.stringify({
                answers: answersArray
            }));
            
            const submitResponse = await fetch(`${config.apiBaseUrl}/quiz-participation/${sessionData.sessionId}/submit`, {
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
                
                // Show success notification
                showNotification(submitData.message || "Quiz submitted successfully!");
                
                // Disable all options
                const options = document.querySelectorAll('.option input');
                options.forEach(option => {
                    option.disabled = true;
                });
                
                // Disable submit buttons
                submitQuizBtn.disabled = true;
                submitQuizBottomBtn.disabled = true;
                submitQuizBtn.innerHTML = '<i class="fas fa-check-circle"></i> Quiz Submitted';
                submitQuizBottomBtn.innerHTML = '<i class="fas fa-check-circle"></i> Quiz Submitted';
                
                // Stop timer
                clearInterval(timerInterval);
            } else {
                // Show error message
                const errorData = await submitResponse.json();
                console.error('Submit quiz API error:', errorData);
                errorEl.textContent = errorData.message || "Failed to submit quiz. Please try again.";
                errorEl.classList.remove('hidden');
                successEl.classList.add('hidden');
                
                // Show error notification
                showNotification(errorData.message || "Failed to submit quiz", true);
                
                // Reset button state
                submitQuizBtn.disabled = false;
                submitQuizBottomBtn.disabled = false;
                submitQuizBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Submit Quiz';
                submitQuizBottomBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Submit Quiz';
            }
        } catch (error) {
            // Show generic error message
            console.error('Error submitting quiz:', error);
            errorEl.textContent = "An error occurred while submitting the quiz.";
            errorEl.classList.remove('hidden');
            successEl.classList.add('hidden');
            
            // Show error notification
            showNotification("An error occurred while submitting the quiz", true);
            
            // Reset button state
            submitQuizBtn.disabled = false;
            submitQuizBottomBtn.disabled = false;
            submitQuizBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Submit Quiz';
            submitQuizBottomBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Submit Quiz';
            
            console.error(error);
        }
    }
});