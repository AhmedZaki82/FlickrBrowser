package ahmedzaki.com;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

public class MainActivity extends AppCompatActivity implements GetFlickrJsonData.OnDataAvailableListener {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: starts");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        GetRawData getRawData = new GetRawData(this);
//        getRawData.execute("https://www.flickr.com/services/feeds/photos_public.gne?" +
//                "tags=android,nougat,sdk&tagmode=any&format=json&nojsoncallback=1");



        Log.d(TAG, "onCreate: ends");
    }

    @Override
    protected void onResume() {

        Log.d(TAG, "onResume: starts");

        super.onResume();

        GetFlickrJsonData getFlickrJsonData = new GetFlickrJsonData("https://www.flickr.com/services/feeds/photos_public.gne",
                "en-us", true, this);
        //getFlickrJsonData.executeOnSameThread("android, nougat");

        Log.d(TAG, "onResume: GetFlickrJsonData created!");

        /* GetFlickrJsonData.doInBackGround method runs the runInSameThread method of GetRawData which DOWNLOAD the data and
        passes it to onPostExecute when it's done ---> onPostExecute(doInBackground(s)); , NOW onPostExecute then calls the
        callback interface to run onDownloadComplete method in GetFlickrJsonData  */
        getFlickrJsonData.execute("android, nougat");

        Log.d(TAG, "onResume: end");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.d(TAG, "onCreateOptionsMenu() returned: " + true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.d(TAG, "onOptionsItemSelected() returned: returned");

            return true;
        }

        Log.d(TAG, "onOptionsItemSelected() returned: returned");
        return super.onOptionsItemSelected(item);
    }

    public void onDataAvailable(List<Photo> data, DownloadStatus status) {

        if (status == DownloadStatus.OK) {

            Log.d(TAG, "onDataAvailable: The Data is ---> " + data);
        } else {

            Log.d(TAG, "onDataAvailable: Error ---> " + status);
        }
    }
}
