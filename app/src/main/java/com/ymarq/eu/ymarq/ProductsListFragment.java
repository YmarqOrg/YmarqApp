package com.ymarq.eu.ymarq;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;


/**
 * Created by eu on 11/7/2014.
 */

/**
 * A placeholder fragment containing a simple view.
 */
public class ProductsListFragment extends Fragment {

    //camera take pictures
    int TAKE_PHOTO_CODE = 0;
    public static int count=0;
    String dir;
    static String LOGGER_TAG = "LOGGERWEB";
    //View rootView;
    ArrayAdapter<String> productsAdapter;


    public ProductsListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        //camera
        //here,we are making a folder named picFolder to store pics taken by the camera using this application
        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picFolder/";
        File newdir = new File(dir);
        newdir.mkdirs();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        // Inflate the menu; this adds items to the action
        inflater.inflate(R.menu.menu_refresh, menu);
        inflater.inflate(R.menu.menu_camera, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh)
        {
            //"http://api.openproductsmap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7"
            GetDataFromCloud(1222222222);
            return true;
        }
        else if (id == R.id.action_camera)
        {
            TakePicture();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void TakePicture() {
        // here,counter will be incremented each time,and the picture taken by camera will be stored as 1.jpg,2.jpg and likewise.
        count++;
        String file = dir+count+".jpg";
        File newfile = new File(file);
        try {
            newfile.createNewFile();
        } catch (IOException e) {}

        Uri outputFileUri = Uri.fromFile(newfile);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TAKE_PHOTO_CODE && resultCode == Activity.RESULT_OK) {
            Log.d("CameraDemo", "Pic saved");

        }
    }

    private void GetDataFromCloud(int cityCode) {
        FetchProductsTask fetchGarrageTask = new FetchProductsTask();
        fetchGarrageTask.execute(String.valueOf(cityCode));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        List<String> weekForecast = new ArrayList<String>();

        productsAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_product,R.id.list_item_products_textview,weekForecast);

        ListView lv1 = (ListView) rootView.findViewById(R.id.listview_products_list);

        lv1.setAdapter(productsAdapter);

        GetDataFromCloud(1111111111);


        String possibleEmail ="";
        String mPhoneNumber ="";

        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(rootView.getContext()).getAccounts();
        for (Account account : accounts) {

            //String acname = account.name;
            //String actype = account.type;

            //if(actype.equals("com.whatsapp")){
            //    mPhoneNumber = account.name;
            //}
            //Toast.makeText(rootView.getContext(), "Phone :" + acname,
            //        Toast.LENGTH_SHORT).show();

            if (emailPattern.matcher(account.name).matches()) {
                possibleEmail = account.name;
            }
        }

        if (mPhoneNumber!= "") {
            TelephonyManager tMgr = (TelephonyManager) rootView.getContext().getSystemService(Context.TELEPHONY_SERVICE);
            mPhoneNumber = tMgr.getLine1Number();
        }
        //Toast.makeText(rootView.getContext(), "Phone :" + mPhoneNumber,
                //Toast.LENGTH_SHORT).show();

        Toast.makeText(rootView.getContext(), "Loging in as :" + possibleEmail,
                Toast.LENGTH_SHORT).show();

        Log.i("EMAIL_LOG", "MainEmailAccount: "+ possibleEmail);

        //boolean resultLogin = connect2("1005", possibleEmail);
        //postData();

        //String parameters = "Id=1051&Email=someval@gmail.com";
        //String url = "http://ymarq.azurewebsites.net/home/Logon";
        //requestUrl(url,parameters);

        //LogonTask t = new LogonTask();
        //t.execute("");

        PostDataFromCloud(30);

        boolean resultLogin =true;
        if (resultLogin) {
            Toast.makeText(rootView.getContext(), "Logged in:" + possibleEmail,
                    Toast.LENGTH_SHORT).show();
        }

        //TextView emailView = (TextView) rootView.findViewById(R.id.emailview_forecast);
        //emailView.setText("Email:"+ possibleEmail);;

        return rootView;
    }

    private void UpdateProductsListView(String[] formatedData) {

        productsAdapter.clear();
        productsAdapter.addAll(formatedData);
    }


    private class FetchProductsTask extends AsyncTask<String, Integer, String[]> {

        private final String LOG_TAG =FetchProductsTask.class.getSimpleName();

        //private String[] forecastArray;

        protected String[] doInBackground(String... userCodes)
        {
            //String email = connectAndGetUserEmail("1111111111");

            //Log.i("EMAIL_LOG2", "GetEmailAccount: "+ email);

            return connectAndGetProductsData(userCodes);
            //return connectMock("",7);
        }


        private String[] connectAndGetProductsData(String[] userCodes) {
            int count = userCodes.length;
            String[] productsArray;
            //for (int i = 0; i < count; i++) {
            //    totalSize += Downloader.downloadFile(urls[i]);
            //    publishProgress((int) ((i / (float) count) * 100));
            //    // Escape early if cancel() is called
            //    if (isCancelled()) break;
            //}
            //return totalSize;

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            try {
                //http://54.200.232.223:8080/photos/GetProducts/111111111

                String myUrl = getServerUrl(userCodes[0]);

                URL url = new URL(myUrl);//urls);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return  null;
                }
                forecastJsonStr = buffer.toString();
                Log.i(LOG_TAG, "ProductResult: " + forecastJsonStr);
                //productsArray = getProductMock(forecastJsonStr, 7);
                productsArray = getProductDataFromJson(forecastJsonStr,7);
                Log.i(LOG_TAG, "ProductResult: "+ forecastJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
// If the code didn't successfully get the products data, there's no point in attempting
// to parse it.
                return null;
            }
            catch (JSONException e) {
                Log.e(LOG_TAG, "Error ", e);
// If the code didn't successfully parse the products data, there's no point in attempting
// to parse it.
                return null;
            }
            finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return productsArray;
        }

        private String getServerUrl(String userCode) {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .encodedAuthority("54.200.232.223:8080")
                    .appendPath("photos")
                    .appendPath("GetProducts")
                    .appendPath(userCode);
            //.appendPath("2.5")add version
            //.appendQueryParameter("id", userCodes[0])
            //.appendQueryParameter("mode", "json")
            //.appendQueryParameter("units","metric")
            //.appendQueryParameter("cnt","7");
            //.fragment("section-name");
            String myUrl = builder.build().toString();
            Log.i(LOG_TAG, "BUILD_URL: " + myUrl);
            return myUrl;
        }

        private String getServerUrl2(String userCode) {
            //http://ymarq.azurewebsites.net/home/Login?userId=111111111
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("ymarq.azurewebsites.net")
                    .appendPath("home")
                    .appendPath("Login")
                    .appendQueryParameter("userId", userCode);
            String myUrl = builder.build().toString();
            Log.i(LOG_TAG, "BUILD_URL: " + myUrl);
            return myUrl;
        }

        private String getServerUrl3(String userCode) {
            //http://ymarq.azurewebsites.net/home/getproducts?userId=1111111111
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("ymarq.azurewebsites.net")
                    .appendPath("home")
                    .appendPath("getproducts")
                    .appendQueryParameter("userId", userCode);
            String myUrl = builder.build().toString();
            Log.i(LOG_TAG, "BUILD_URL: " + myUrl);
            return myUrl;
        }


        /* The date/time conversion code is going to be moved outside the asynctask later,
        * so for convenience we're breaking it out into its own method now.
        */
        private String getReadableDateString(long time){
// Because the API returns a unix timestamp (measured in seconds),
// it must be converted to milliseconds in order to be converted to valid date.
            Date date = new Date(time * 1000);
            SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
            return format.format(date).toString();
        }

        /**
         * Prepare the products high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
// For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }


        private String[] getProductMock(String forecastJsonStr, int numDays)
                throws JSONException {

            String js = "[{\"Description\":\"Suzuki Swift\",\"Hashtag\":\"Nice car\",\"Id\":\"e7b6646b-4718-4abf-8260-73188d395c30\",\"Image\":\"\",\"PublisherId\":\"\"}]";
            String[] results = new String[1];
            results[0] = "moshe";

            return results;
        }

        private String[] connectMock(String forecastJsonStr, int numDays)
                 {
            String js = "[{\"Description\":\"Suzuki Swift\",\"Hashtag\":\"Nice car\",\"Id\":\"e7b6646b-4718-4abf-8260-73188d395c30\",\"Image\":\"\",\"PublisherId\":\"\"}]";
            String[] results = new String[1];
            results[0] = "moshe";

            return results;
        }

        private String getEmailFromJson(String jSonResultString)
                throws JSONException {

            JSONObject listJson = new JSONObject(jSonResultString);

            String email = listJson.getString("Email");

            return email;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy: constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getProductDataFromJson(String jSonResultString, int numDays)
                throws JSONException {

            //[{"Description":"Suzuki Swift","Hashtag":"Nice car","Id":"e7b6646b-4718-4abf-8260-73188d395c30","Image":"","PublisherId":""}]
            // These are the names of the JSON objects that need to be extracted.
            final String OWM_DESCRIPTION = "Description";
            final String OWM_HASHTAG = "Hashtag";
            final String OWM_ID = "Id";
            final String OWM_IMAGE = "Image";

            final String OWM_LIST = "";

            //JSONObject listJson = new JSONObject(forecastJsonStr);
            //JSONArray productArray = listJson.getJSONArray(OWM_LIST);
            JSONArray productArray = new JSONArray(jSonResultString);

            String[] resultStrs = new String[productArray.length()];
            for (int i = 0; i < productArray.length(); i++) {

                String description;
                String hashtag;

                // Get the JSON object representing the day
                JSONObject product = productArray.getJSONObject(i);

                description = product.getString(OWM_DESCRIPTION);
                hashtag = product.getString(OWM_HASHTAG);

                resultStrs[i] = description + " - " + hashtag;
            }
            return resultStrs;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String[] forecastArray) {
            UpdateProductsListView(forecastArray);
            //showDialog("Downloaded " + result + " bytes");
        }
    }
























    /// POST DATA AND LOGINS

    private void PostDataFromCloud(int cityCode) {
        LogonTask lt = new LogonTask();
        lt.execute("");
    }


    public class LogonTask extends AsyncTask<String, Integer, String[]> {

        private final String LOG_TAG =LogonTask.class.getSimpleName();

        protected String[] doInBackground(String... userCodes)
        {
            String parameters = "Id=1091&Email=someval1091@gmail.com";
            String url = "http://ymarq.azurewebsites.net/home/Logon";
            requestUrl(url,parameters);

            return null;
        }




        public String requestUrl(String url, String postParameters)
        {
            if (Log.isLoggable(LOGGER_TAG, Log.INFO)) {
                Log.i(LOGGER_TAG, "Requesting service: " + url);
            }

            //disableConnectionReuseIfNecessary();

            HttpURLConnection urlConnection = null;
            try {
                // create connection
                URL urlToRequest = new URL(url);
                urlConnection = (HttpURLConnection) urlToRequest.openConnection();
                urlConnection.setConnectTimeout(10000);
                urlConnection.setReadTimeout(10000);

                // handle POST parameters
                if (postParameters != null) {

                    if (Log.isLoggable(LOGGER_TAG, Log.INFO)) {
                        Log.i(LOGGER_TAG, "POST parameters: " + postParameters);
                    }

                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setFixedLengthStreamingMode(
                            postParameters.getBytes().length);
                    urlConnection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");

                    //send the POST out
                    PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                    out.print(postParameters);
                    out.close();
                }

                // handle issues
                int statusCode = urlConnection.getResponseCode();
                if (statusCode != HttpURLConnection.HTTP_OK) {
                    // throw some exception
                }

                // read output (only for GET)
                if (postParameters != null) {
                    return null;
                } else {
                    InputStream in =
                            new BufferedInputStream(urlConnection.getInputStream());
                    //return getResponseText(in);
                }


            } catch (MalformedURLException e) {
                // handle invalid URL
            } catch (SocketTimeoutException e) {
                // hadle timeout
            } catch (IOException e) {
                // handle I/0
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return null;
        }






        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String[] forecastArray) {

            //showDialog("Downloaded " + result + " bytes");
        }
    }
}

