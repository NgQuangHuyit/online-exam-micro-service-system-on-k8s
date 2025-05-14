/**
 * Configuration file for frontend application
 * Contains environment-specific settings
 */

const config = {
  // API base URL - change this when deploying to different environments
  apiBaseUrl: 'http://quizapp.ptit.local:30080/api',
  // apiBaseUrl: 'http://localhost:8080/api',
  // Other configuration values can be added here
  // For example:
  // timeoutDuration: 30000,
  // maxRetries: 3
};

// Freeze the object to prevent modifications
Object.freeze(config);

// Export configuration
export default config;