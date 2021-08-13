package com.eceakilli.artbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.eceakilli.artbook.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    ArrayList<Art> artArrayList;
    ArtAdapter artAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);

        artArrayList=new ArrayList<>();

        binding.rcyclerView.setLayoutManager(new LinearLayoutManager(this));
        artAdapter=new ArtAdapter(artArrayList);
        binding.rcyclerView.setAdapter(artAdapter);

        getData();

    }
    private void getData(){
        try {

            SQLiteDatabase sqLiteDatabase=this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            Cursor cursor=sqLiteDatabase.rawQuery("SELECT * FROM arts",null);
            int nameIndex=cursor.getColumnIndex("artname");
            int idIndex=cursor.getColumnIndex("id");//id de çektik çünkü diger verileri listelemek istedik

            while (cursor.moveToNext()){
                String name=cursor.getString(nameIndex);
                int id=cursor.getInt(idIndex);
                Art art=new Art(name,id);
                artArrayList.add(art);
            }
            //verilere yeni listelenecek sey eklendi güncelle demek
            artAdapter.notifyDataSetChanged();
            cursor.close();


        }catch (Exception e){

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //xml de olusturulan menuyu koda baglama
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.art_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
        //menuye tıklandığında ne yapılmasını istediğim alan

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //birden fazla olsaydı elseif ile devam ettiridin fakat bizim menumuz 1 secenekli
        if (item.getItemId()==R.id.add_art){
            //neye tıkladıgını secebilmek için info-new yapıyoruz
            Intent intent=new Intent(this,ArtActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }
}