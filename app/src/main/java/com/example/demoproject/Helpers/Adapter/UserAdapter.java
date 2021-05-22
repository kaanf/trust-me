package com.example.demoproject.Helpers.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demoproject.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {

    Context context;
    List<UserInformation> userList;

    public UserAdapter(Context context, List<UserInformation> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_information, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        UserInformation user = userList.get(position);
        holder.name.setText(user.getName());
        holder.id.setText(user.getId());
        Glide
                .with(context)
                .load(user.getImage())
                .centerCrop()
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, id;
        ImageView image;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            id = itemView.findViewById(R.id.id);
            image = itemView.findViewById(R.id.profile_image);
        }
    }

}