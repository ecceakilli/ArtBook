package com.eceakilli.artbook;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eceakilli.artbook.databinding.RcyclerRowBinding;

import java.io.Serializable;
import java.util.ArrayList;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder> {

    //listemde kaç tane gözükecek kararı
    ArrayList <Art> artArrayList;
    public ArtAdapter(ArrayList <Art> artArrayList){
        this.artArrayList=artArrayList;
    }

    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RcyclerRowBinding rcyclerRowBinding=RcyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false );
        return new  ArtHolder(rcyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, int position) {
        holder.binding.rcyclerViewText.setText(artArrayList.get(position).name);
        holder.artId = artArrayList.get(position).id;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(holder.itemView.getContext(),ArtActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("artId",holder.artId);
                holder.itemView.getContext().startActivity(intent);
             //   intent.putExtra("artId",artArrayList.get(position).id);


            }
        });

    }

    @Override
    public int getItemCount() {
        //listemde kaç tane gözükecek kararı
        return artArrayList.size();
    }

    public class ArtHolder extends RecyclerView.ViewHolder{
       private RcyclerRowBinding binding;
       public int artId;

        public ArtHolder(RcyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
