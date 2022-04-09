print('Start #################################################################');
db = db.getSiblingDB('OpenDelosLive');
db.createUser(
    {
        user: 'mih',
        pwd: 'admin123!',
        roles: [{ role: 'readWrite', db: 'OpenDelosLive' }],
    },
);
db.createCollection('users');
print('END #################################################################');