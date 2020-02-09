package ahmedzaki.com;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*This class implements onDownloadComplete so it can get callbacks from GetRawData, and it also defines its own interface

OnDataAvailableListener so that it can send a callback to MainActivity when we have data to send back to it.*/

class GetFlickrJsonData extends AsyncTask<String, Void, List<Photo>> implements GetRawData.OnDownloadCompleteListener {

    private static final String TAG = "GetFlickrJsonData";

    // Here is where we will store our photos that will parse from json data.
    private List<Photo> mPhotoList = null;

    // This is the base URL without any parameters, like the search, tag, and format.
    // The RAW link that will get data from Flickr.
    private String mBaseURL;

    // Here we will specified the language.
    private String mLanguage;

    // That will allow us to choose between matching ALL the search terms OR ANY of them.
    private boolean mMatchAll;

    private boolean runningOnSameThread = false;

    // Callback object.
    //Create a field to store the callback object
    private final OnDataAvailableListener mCallback;

    // Define the interface.
    // Create the interface.
    interface OnDataAvailableListener {
        void onDataAvailable(List<Photo> data, DownloadStatus status);
    }

    @Override
    protected List<Photo> doInBackground(String... params) {

        Log.d(TAG, "doInBackground: starts.");

        String destinationUri = createUri(params[0],mLanguage,mMatchAll);

        GetRawData getRawData = new GetRawData(this);
        getRawData.runInSameThread(destinationUri);

        Log.d(TAG, "doInBackground: end.");
        return mPhotoList;
    }

    @Override
    protected void onPostExecute(List<Photo> photos) {
//        super.onPostExecute(photos);

        if (mCallback != null) {

            mCallback.onDataAvailable(mPhotoList,DownloadStatus.OK);
        }

        Log.d(TAG, "onPostExecute: ends");
    }




    // The constructor  
    public GetFlickrJsonData(String baseURL, String language, boolean matchAll, OnDataAvailableListener callback) {

        // To Confirm that the constructor has been called.
        Log.d(TAG, "GetFlickrJsonData Called");

        mBaseURL = baseURL;
        mLanguage = language;
        mMatchAll = matchAll;
        mCallback = callback;
    }


    void executeOnSameThread(String searchCriteria) {

        Log.d(TAG, "executeOnSameThread: started");

        runningOnSameThread = true;

        /* Before GetRawData is created and its execute method called,
        we BUILD up the URL so that it's got the correct parameters, we will do that in the
        "createUri()" method.*/

        String destinationUri = createUri(searchCriteria, mLanguage, mMatchAll);

        GetRawData getRawData = new GetRawData(this);
        getRawData.execute(destinationUri);

        Log.d(TAG, "executeOnSameThread: end");
    }

    private String createUri(String searchCriteria, String lang, boolean matchAll) {

        Log.d(TAG, "createUri: starts");

        return Uri.parse(mBaseURL).buildUpon()
                .appendQueryParameter("tags", searchCriteria)
                .appendQueryParameter("tagmode", matchAll ? "ALL" : "ANY")
                .appendQueryParameter("lang", lang)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("nojsoncallback", "1")
                .build().toString();
    }

    /* This method -onDownloadComplete- will be implemented on the onPostExecute(String s); in the
    GetRawData Class, which s is the output of the doInBackgound(String... strings); which strings
    is the argument of the ---> getRawData.execute(destinationUri), in other word --->
    strings = destinationUri.*/
    @Override
    public void onDownloadComplete(String data, DownloadStatus status) {

        Log.d(TAG, "onDownloadComplete: Starts. Status = " + status);

        if (status == DownloadStatus.OK) {

            mPhotoList = new ArrayList<>();

            try {
                JSONObject jsonData = new JSONObject(data);
                JSONArray itemsArray = jsonData.getJSONArray("items");

                for (int i = 0; i < itemsArray.length(); i++) {

                    JSONObject jsonPhoto = itemsArray.getJSONObject(i);
                    String title = jsonPhoto.getString("title");
                    String author = jsonPhoto.getString("author");
                    String authorId = jsonPhoto.getString("author_id");
                    String tags = jsonPhoto.getString("tags");

                    JSONObject jsonMedia = jsonPhoto.getJSONObject("media");
                    String photoUrl = jsonMedia.getString("m");

                    String link = photoUrl.replaceFirst("_m.", "_b.");

                    /* Collect the desired information in one object */
                    Photo photoObject = new Photo(title, author, authorId, link, tags, photoUrl);

                    /* Add the photo objects in an arrayList */
                    mPhotoList.add(photoObject);

                    Log.d(TAG, "onDownloadComplete " + photoObject.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "onDownloadComplete: Error processing json Data " + e.getMessage());
                status = DownloadStatus.FAILED_OR_EMPTY;
            }
        }

        if (runningOnSameThread && mCallback != null) {

            /* We need to notify the calling class that everything's done and send it the list of
            photos that we've actually created,now inform the caller that processing is done
            - possibly returning null if there was an error. So either way "succeed or not" we will
            callback the calling class and the calling class will deal with both results.*/

            mCallback.onDataAvailable(mPhotoList,status);
        }

        Log.d(TAG, "onDownloadComplete: end");

    }
}
