# A1_SE751_G27
A gallery app to filter out non-face pictures.

**Set Up**
1. Clone Repository

2. Download  dataset.tar.bz2 file from this link skip to step 4 if you have a dataset already:

https://github.com/Nemeritz/A1_SE751_G27/releases 

3. unzip.

4. Make sure the dataset folder is named "Dataset".

5. change to root directory of project.

6. Set environmental variable FACEGALLERY_DATASET to your Test file path **without** the trailing slash.
e.g. Users/Chris/Desktop

7. Follow instructions from the following link to get a json file for google API:

https://cloud.google.com/genomics/downloading-credentials-for-api-access

8. Set environmental variable GOOGLE_APPLICATION_CREDENTIALS to your downloaded json file.
e.g Users/Chris/Desktop/secrets.json

9. gradle run

10. Press 5 to run GUI

**Troubleshooting**

If the application gets does not progress for the thumbnail generation and the darkening & blurring then try to restart the application. If restarting the application does not work then try to change the amount of Images in the dataset.

**Caution**

Do not try to edit the dataset folder while the progress bars have not reached 100% as that will break the application.
