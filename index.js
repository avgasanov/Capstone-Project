'use strict';

const functions = require('firebase-functions');

const gcs = require('@google-cloud/storage')();
const spawn = require('child-process-promise').spawn;
const path = require('path');
const os = require('os');
const fs = require('fs');

const admin = require('firebase-admin');
admin.initializeApp();

var data = require('./path/to/testData.json');

exports.addPhotoIdToCarTrg = functions.database.ref('/debug_database/carPhotos/{carPhotoId}')
    .onCreate((snapshot, context) => {
        return addPhotoIdToCarTrgImp('debug_database', snapshot, context);
    });

exports.likeUnlike = functions
    .region('europe-west1')
    .https
    .onCall((data, context) => {
    console.log('testHappens');
        return likeUnlikeImp('debug_database', data, context);
    });


exports.notifyNewPhoto = functions.database.ref('/debug_database/notifications/{userId}/{notificationId}')
    .onCreate((snapshot, context) => {
        return notifyNewPhotoImp('debug_database', snapshot, context);
    });


// [START generateThumbnail] FIREBASE EXAMPLE!!!
/**
 * When an image is uploaded in the Storage bucket We generate a thumbnail automatically using
 * ImageMagick.
 */
// [START generateThumbnailTrigger]
exports.generateThumbnail = functions.storage.object().onFinalize((object) => {
// [END generateThumbnailTrigger]
    // [START eventAttributes]
    const fileBucket = object.bucket; // The Storage bucket that contains the file.
    const filePath = object.name; // File path in the bucket.
    const contentType = object.contentType; // File content type.
    const metageneration = object.metageneration; // Number of times metadata has been generated. New objects have a value of 1.
    // [END eventAttributes]

    let thumbPath;

    // [START stopConditions]
    // Exit if this is triggered on a file that is not an image.
    if (!contentType.startsWith('image/')) {
        console.log('This is not an image.');
        return null;
    }

    // Get the file name.
    const fileName = path.basename(filePath);
    // Exit if the image is already a thumbnail.
    if (fileName.startsWith('thumb_')) {
        console.log('Already a Thumbnail.');
        return null;
    }
    // [END stopConditions]

    // [START thumbnailGeneration]
    // Download file from bucket.
    const bucket = gcs.bucket(fileBucket);
    const tempFilePath = path.join(os.tmpdir(), fileName);
    const metadata = {
        contentType: contentType,
    };
    return bucket.file(filePath).download({
        destination: tempFilePath,
    }).then(() => {
        console.log('Image downloaded locally to', tempFilePath);
        // Generate a thumbnail using ImageMagick.
        return spawn('convert', [tempFilePath, '-thumbnail', '200x200>', tempFilePath]);
    }).then(() => {
        console.log('Thumbnail created at', tempFilePath);
        // We add a 'thumb_' prefix to thumbnails file name. That's where we'll upload the thumbnail.
        const thumbFileName = `thumb_${fileName}`;
        const thumbFilePath = path.join(path.dirname(filePath), thumbFileName);
        // Uploading the thumbnail.
        thumbPath = thumbFilePath;
        return bucket.upload(tempFilePath, {
            destination: thumbFilePath,
            metadata: metadata,
        });
        // Once the thumbnail has been uploaded delete the local file to free up disk space.
    }).then(() => fs.unlinkSync(tempFilePath));
    // [END thumbnailGeneration]
});
// [END generateThumbnail]


function addPhotoIdToCarTrgImp(database, snapshot, context) {
    const plateNum = cleanPlate(snapshot.val().plateNum);
    const catcherId = snapshot.val().catcherId;
    const carPhotoId = snapshot.key;
    const catcherUsername = snapshot.val().catcherName;
    const timestamp = admin.database.ServerValue.TIMESTAMP;
    const photoUrl = snapshot.val().photoUrl;
    let updates = {};

    updates['/' + database + '/carPhotos/' + carPhotoId + '/timestamp'] = timestamp;
    updates['/' + database + '/searchIndex/catchedByUser/' + catcherId + '/' + carPhotoId] = true;
    updates['/' + database + '/carPhotos/' + carPhotoId + '/plateNum'] = plateNum;
    updates['/' + database + '/searchIndex/photosByPlate/' + plateNum + '/' + carPhotoId] = true; 

        return admin
                .database()
                .ref('/' + database + '/searchIndex/plateNums/' + plateNum)
                .once('value')
        .then(snapshot => {
            const promisesArray = [];
            snapshot.forEach(child => {
                const carKey = child.key;
                updates['/' + database + '/cars/' + carKey + '/photos/' + carPhotoId] = true;
                promisesArray
                    .push(admin.database().ref('/' + database + '/cars/' + carKey + '/userId')
                            .once('value'));
            });
            return Promise.all(promisesArray);
        })
        .then(snapshot => {
            const promisesArray = [];
            snapshot.forEach(child => {
                const userId = child.val();
                const carKey = child.ref.parent.key;
                const notifKey = admin
                                .database()
                                .ref('/' + database + '/notifications/' + userId)
                                .push()
                                .key;
                updates['/' + database + '/notifications/' + userId + '/' + notifKey + '/carPhotoId'] = carPhotoId;
                updates['/' + database + '/notifications/' + userId + '/' + notifKey + '/catcherId'] = catcherId;
                updates['/' + database + '/notifications/' + userId + '/' + notifKey + '/timestamp'] = timestamp;
                updates['/' + database + '/notifications/' + userId + '/' + notifKey + '/type'] = 'catched';
                updates['/' + database + '/notifications/' + userId + '/' + notifKey + '/carId'] = carKey;
                updates['/' + database + '/notifications/' + userId + '/' + notifKey + '/catcherName'] = catcherUsername;
                updates['/' + database + '/notifications/' + userId + '/' + notifKey + '/photoUrl'] = photoUrl;
                promisesArray.push(admin.database().ref('/' + database + '/users/' + userId).once('value'));
            });
            return Promise.all(promisesArray);
        }).then(snapshot => {
            const promisesArray = [];
            
            snapshot.forEach(child => {
                const profilePicture = child.child('profilePicture').val();
                const username = child.child('username').val();
                const userId = child.key;
                updates ['/' + database + '/carPhotos/' + carPhotoId + '/' + 'catchedUserIds/' + userId + '/profilePicture'] = profilePicture;
                updates['/' + database + '/carPhotos/' + carPhotoId + '/' + 'catchedUserIds/' + userId + '/username'] = username;
                updates['/' + database + '/searchIndex/userPhotos/' + userId + '/' + carPhotoId] = timestamp;
            });

            return admin.database().ref().update(updates);
            });
}



function likeUnlikeImp(database, data, context) {
    const carPhotoId = data.carPhotoId;
    const profilePicture = data.profilePicture;
    const username = data.username;
    const userId = context.auth.uid;
    let result = {};
    let likesCount;

    return admin
        .database()
        .ref('/' + database + '/searchIndex/likedByUser/' + userId + '/' + carPhotoId).once('value')
        .then(snapshot => {
        if ((snapshot.exists() && data.liked === true) ||
            (!snapshot.exists() && data.liked === false)) {
            result.alreadyDone = true;
            console.log('already done throwing result');
            throw result;
        } else {
            const updates = {};
            if (data.like === true) {
                updates['/' + database + '/searchIndex/likedByUser/' + userId + '/' + carPhotoId] = true;
                updates['/' + database + '/likes/' + carPhotoId + '/' + userId + '/username'] = username;
                updates['/' + database + '/likes/' + carPhotoId + '/' + userId + '/profilePicture'] = profilePicture;
            } else {
                updates['/' + database + '/likes/' + carPhotoId + '/' + userId] = null;
                updates['/' + database + '/searchIndex/likedByUser/' + userId + '/' + carPhotoId] = null;
            }
            return admin.database().ref().update(updates);
        }
        }).then( (result) => {
                console.log('entering transaction block');
                return admin
                    .database()
                    .ref('/' + database + '/carPhotos/' + carPhotoId + '/likeCount')
                    .transaction(likeCount => {
                        console.log('start transaction');
                        if (data.like === true) {
                            likesCount = (likeCount || 0) + 1;
                            return likesCount;
                        } else {
                            likesCount = (likeCount || 0) - 1;
                            return likesCount;
                        }
                    });
        }).then( () => {
            console.log('after transaction then');
            result.likeCount = likesCount;
            return result;
        }).catch(s => {
            console.log('error happened');
            console.log(s);
            if (s.alreadyDone === true)
            console.log('already done');
            return s;
        });
}

function notifyNewPhotoImp(database, snapshot, context) {
    const userId = context.params.userId;
    const notificationId = context.params.notificationId;
    const carKey = snapshot.val().carKey;
    const timestamp = snapshot.val().timestamp;
    const catcherId = snapshot.val().catcherId;
    const carPhotoId = snapshot.val().carPhotoId;
    const photoUrl = snapshot.val().photoUrl;
    let catcherUsername;

    return admin.database().ref('/' + database + '/users/' + catcherId + '/username').once('value')
    .then(username => {
        catcherUsername = username;
        return admin
                .database()
                .ref('/' + database + '/tokens/' + userId)
                .once('value')
    }).then(snapshot => {
        const promisesArray = [];
        snapshot.forEach(child => {
            console.log(child.key);
            let notifiedToken = child.key;
            var message = {
                'data': {
                    title: 'You are cathed',
                    body: catcherUsername + ' catched you!',
                    carKey: String.toString(carKey),
                    timestamp: String.toString(timestamp),
                    catcherId: String.toString(catcherId),
                    carPhotoId: String.toString(carPhotoId),
                    photoUrl: photoUrl
                },
                token: child.key
            };
            promisesArray.push(admin.messaging().send(message));
        });
        return Promise.all(promisesArray);
    }) 
    .then((response) => {
        console.log('Successfully sent message:', response);
        return null;
      })
      .catch((error) => {
        console.log('Error sending message:', error);
        return null;
      });
}


function cleanPlate(plateNum) {
    console.log('before cleaning platenum was ' + plateNum);
    const result = plateNum.trim().replace(/[\W_]+/g,"").toUpperCase();
    console.log('after cleanin platenum is ' + result);
    return result;
}



