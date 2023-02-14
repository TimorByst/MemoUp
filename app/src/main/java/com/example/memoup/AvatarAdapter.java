package com.example.memoup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder> {
    private final int NUMBER_OF_AVATARS_AVAILABLE = 18;
    private final Context context;
    private final ArrayList<String> avatars = new ArrayList<>();
    private Activity_Profile.OnAvatarSelectedListener listener;

    {
        avatars.add("bear");
        avatars.add("cat");
        avatars.add("cougar");
        avatars.add("dog");
        avatars.add("elephant");
        avatars.add("fox");
        avatars.add("frog");
        avatars.add("koala");
        avatars.add("leopard");
        avatars.add("lion");
        avatars.add("monkey");
        avatars.add("panda");
        avatars.add("panther");
        avatars.add("rat");
        avatars.add("red_Panda");
        avatars.add("rhino");
        avatars.add("tiger");
        avatars.add("wolf");
    }


    public AvatarAdapter(Context context) {
        this.context = context;
    }

    public void setListener(Activity_Profile.OnAvatarSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public AvatarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.avatar_layout, parent, false);
        return new AvatarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AvatarViewHolder holder, int position) {
        int avatarResourceId = context.getResources().getIdentifier(avatars.get(position),
                "drawable", context.getPackageName());
        holder.avatarImageView.setImageResource(avatarResourceId);

        holder.avatarImageView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAvatarSelected(avatarResourceId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return NUMBER_OF_AVATARS_AVAILABLE; // The number of avatars available
    }

    static class AvatarViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImageView;

        AvatarViewHolder(View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avatar_image_view);
        }
    }
}

