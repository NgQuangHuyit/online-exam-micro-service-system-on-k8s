// Tạo database resultdb
db = db.getSiblingDB('resultdb');

// Tạo user với quyền readWrite cho database resultdb
db.createUser({
    user: 'result_user',
    pwd: 'result_password',
    roles: [
        {
            role: 'readWrite',
            db: 'resultdb'
        }
    ]
});

// Tạo collection 'results'
db.createCollection('results');

// Tạo indexes để tối ưu truy vấn
db.results.createIndex({ "examSessionId": 1 }, { unique: true });
db.results.createIndex({ "studentId": 1, "quizId": 1 });
db.results.createIndex({ "createdAt": 1 });

print('MongoDB initialized for Result Service');