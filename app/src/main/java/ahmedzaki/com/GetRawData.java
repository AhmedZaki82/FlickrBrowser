package ahmedzaki.com;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

enum DownloadStatus {IDLE, PROCESSING, NOT_INITIALISED, FAILED_OR_EMPTY, OK}



class GetRawData extends AsyncTask<String , Void, String > {

    private static final String TAG = "GetRawData";

    private DownloadStatus mDownloadStatus;


    interface OnDownloadCompleteListener {

        void onDownloadComplete(String data, DownloadStatus status);
    }

    private final OnDownloadCompleteListener callback;



    public GetRawData(OnDownloadCompleteListener callback) {

        this.mDownloadStatus = DownloadStatus.IDLE;
        this.callback = callback;

    }


    void runInSameThread(String s){

        Log.d(TAG, "runInSameThread: starts.");


        /* You shouldn't call method that invokes SUPER ---> super.onPostExecute(s);
           from your own code when they're designed to be used as callbacks from another class */

//        onPostExecute(doInBackground(s));

        if (callback != null) {

//            String result = doInBackground(s);
//            callback.onDownloadComplete(result, mDownloadStatus);

            callback.onDownloadComplete(doInBackground(s),mDownloadStatus);

        }

        Log.d(TAG, "runInSameThread: ends.");
    }

    @Override
    protected void onPostExecute(String s) {
//        super.onPostExecute(s);

        Log.e(TAG, "onPostExecute: Parameter is: " + s );

        callback.onDownloadComplete(s,mDownloadStatus);
//        MainActivity mainActivity = new MainActivity();
//        mainActivity.onDownloadComplete(s,mDownloadStatus);

        Log.d(TAG, "onPostExecute: end.");
    }

    @Override
    protected String doInBackground(String... strings) {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        if (strings == null) {

            mDownloadStatus = DownloadStatus.NOT_INITIALISED;
            return null;
        }

        try {

            URL url = new URL(strings[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            StringBuilder builder = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            int response = connection.getResponseCode();
            Log.d(TAG, "doInBackground: Response Code is: --->" + response);

            /*String line;

            while (null != (line = reader.readLine())) {

                builder.append(line).append("\n");

            }*/

            for (String line = reader.readLine(); null != line; line = reader.readLine()) {

                builder.append(line).append("\n");
            }

            mDownloadStatus = DownloadStatus.OK;

            return builder.toString();

        } catch (MalformedURLException e) {

            Log.e(TAG, "doInBackground: Invalid URL " + e.getMessage());
        } catch (IOException e) {

            Log.e(TAG, "doInBackground: IO Exception Reading Data " + e.getMessage());
        } catch (SecurityException e) {

            Log.e(TAG, "doInBackground: Security Exception, Needs Permission? " + e.getMessage());
        }finally {

            if (connection != null) {

                connection.disconnect();
            }

            if (reader != null) {

                try {
                    reader.close();
                } catch (IOException e) {

                    Log.e(TAG, "doInBackground: Error closing stream! " + e.getMessage());
                }
            }
        }

        mDownloadStatus = DownloadStatus.FAILED_OR_EMPTY;
        return null;
    }
}
