package com.eceakilli.artbook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.eceakilli.artbook.databinding.ActivityArtBinding;
import com.eceakilli.artbook.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class ArtActivity extends AppCompatActivity {

    private ActivityArtBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher; //galeriye gitmek için kullan
    ActivityResultLauncher<String> permssionLauncher;      //izini istemek için kullan
    Bitmap selectedImage;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityArtBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);

        registerLauncer();

        database=this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);

        //new mi old mu onu kontrol edeccegiz
        Intent intent=getIntent();
        String info=intent.getStringExtra("info");

        //formu temizle
        if (info.equals("new")){
            //new arts
            binding.nameText.setText("");
            binding.artistText.setText("");
            binding.yearText.setText("");
            binding.btnSave.setVisibility(View.VISIBLE);
            binding.imageView.setImageResource(R.drawable.selectimage);

        }
        //id ye göre görüntüle butonu gizle
        else {
            int artId=intent.getIntExtra("artId",0);
            binding.btnSave.setVisibility(View.INVISIBLE);
            //bana bir id geldi bu idye göre çekip listeleyecegim
            try {
                Cursor cursor=database.rawQuery("SELECT * FROM arts WHERE id=?",new String[]{String.valueOf(artId)});
                int artNameIndex=cursor.getColumnIndex("artname");
                int painterNameIndex=cursor.getColumnIndex("paintername");
                int yearIndex= cursor.getColumnIndex("year");
                int imageIndex= cursor.getColumnIndex("image");

                while (cursor.moveToNext()){
                    binding.nameText.setText(cursor.getString(artNameIndex));
                    binding.artistText.setText(cursor.getString(painterNameIndex));
                    binding.yearText.setText(cursor.getString(yearIndex));

                    byte[] bytes=cursor.getBlob(imageIndex);
                    Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                    binding.imageView.setImageBitmap(bitmap);

                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }


    }

    public void save(View view){

        String name=binding.nameText.getText().toString();
        String artsistName=binding.artistText.getText().toString();
        String year=binding.yearText.getText().toString();

        Bitmap smallImage=makeSmallerImage(selectedImage,300);
        //img kullanabilmek için byte cinsine cevirmeliyiz
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray=outputStream.toByteArray();

//----SQLlite bağlantı
        try {

            database.execSQL("CREATE TABLE IF NOT EXISTS arts(id INT PRIMARY KEY, artname VARCHAR, paintername VARCHAR, year VARCHAR, image BLOB)" );
            String sqlString="INSERT INTO arts(artname,paintername,year,image)VALUES(?,?,?,?)";
            SQLiteStatement sqLiteStatement=database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,name);
            sqLiteStatement.bindString(2,artsistName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();

        }catch (Exception e){
            e.printStackTrace();
            }
        //kayıt yaptıktan sonra geri dönme işlemi yapılacak bu yüzden intent yapılmalı

        Intent intent=new Intent(ArtActivity.this,MainActivity.class);
        //bundan önceki bütün aktiviteleri kapat sadece suan gideceğim activiteyi aç demek
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);



    }
    //resim boyutu sqllite için önemli bu yüzden küçültmelisin.
    public Bitmap makeSmallerImage(Bitmap image, int maxSize){
      //  fotografın dikey ve yataylığına göre bir ayarlama yapılması gerkir
        int width=image.getWidth();//görselin genişliği
        int height= image.getHeight();;//görselin uzunlugu
        float bitmapOran=(float)width/(float)height;

        if (bitmapOran>1){
            //landscape(yatay) image
            width=maxSize;
            height=(int) (width/bitmapOran);
        }else{
            //portrair image
            height=maxSize;
            width=(int) (width*bitmapOran);
        }
        //yeni uzunluklarla olan görseli donduruyor
        return image.createScaledBitmap(image,width,height,true);
    }

    public void selectImage(View view){
        //---------İZİN var mı kontrol edeceğiz
        //Permission_granted=izin verilmiş//Permission_denied=izin verilmemiş

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){ //checkself ile izin kontrol et-'this' bu activityde-hangi izni kullanacağım,manifest dosyasından bak.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                //length_ındefinite:süresiz göster. sonrasında izin ver sekilde buton yap
 // ----snakeBar start
                Snackbar.make(view,"Galeriye girmek için izin gereklidir",Snackbar.LENGTH_INDEFINITE).setAction("İzin ver", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //izin ver butonuna tıklandıgında
                        permssionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

                    }
                }).show();
            }
            else{
                permssionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
//-----snakeBar end

        }
        else{
            //basta izn verilmiş ise direk galeriye git
            //action_pick ile demek istediğimiz galeriye gidip bir görsel alıp gelecegım.
            //galeriye gittik seçildi ne yapacagız ActivityResaultLauncher kullanacagız
            Intent intentToGallery=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);

        }

    }

    private  void registerLauncer(){

        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {//kullanıc galeriye gitti mi?
            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode()==RESULT_OK){//kullanıcı birsey secti
                    Intent intentFromResult=result.getData();
                    if (intentFromResult != null){
                       Uri imageData= intentFromResult.getData();//kullanıcnın sectiği görselin nerede kayıtlı old.

                      //görselleri kayıtta tutmak için bitmap çevirmemiz gerekli try-catch kullanıcya göster
                        try {
                            if (Build.VERSION.SDK_INT >=28){//telefon versyonu 28 ustunde ise bunu yap

                            ImageDecoder.Source source=ImageDecoder.createSource(getContentResolver(),imageData);
                           selectedImage= ImageDecoder.decodeBitmap(source);//aşka yerlerdede kullanacagım icin degiskene attm
                           binding.imageView.setImageBitmap(selectedImage);
                            } else{//28 altında ise
                                selectedImage =MediaStore.Images.Media.getBitmap(ArtActivity.this.getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectedImage);
                            }

                        }catch (Exception e){
                            e.printStackTrace();


                        }
                    }
                }
            }
        });

        permssionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                //kullanıcı izin ver veya vermeye bastı
                if (result){
                   //permission granted
                    Intent intentToGallery=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }
                else{
                    //permission denied
                    Toast.makeText(ArtActivity.this,"Izin gerekli!",Toast.LENGTH_LONG).show();
                }

            }

        });
    }
}