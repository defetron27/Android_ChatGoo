package com.def.max.chatgoo.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.def.max.chatgoo.Models.BotMessageModel;
import com.def.max.chatgoo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BotChattingRecyclerAdapter extends RecyclerView.Adapter<BotChattingRecyclerAdapter.BotChattingViewHolder>
{
    private Context context;
    private ArrayList<BotMessageModel> messages;

    public BotChattingRecyclerAdapter(Context context, ArrayList<BotMessageModel> messages)
    {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public BotChattingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.bot_chatting_layout_items,parent,false);

        return new BotChattingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BotChattingViewHolder holder, int position)
    {
        BotMessageModel model = messages.get(position);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();

        String onlineUserId = null;

        String botId = "61d29dfb150b4378bf63895c95c6c75c";

        if (user != null)
        {
            onlineUserId = user.getUid();
        }

        if (model.getType().equals("text"))
        {
            if (model.getFrom().equals(botId))
            {
                holder.outcomeMessage.setVisibility(View.GONE);

                holder.incomeResponse.setVisibility(View.VISIBLE);
                holder.assistantImg.setVisibility(View.VISIBLE);

                holder.incomeResponse.setText(model.getMessage());
            }
            if (model.getFrom().equals(onlineUserId))
            {
                holder.incomeResponse.setVisibility(View.GONE);
                holder.assistantImg.setVisibility(View.GONE);

                holder.outcomeMessage.setVisibility(View.VISIBLE);

                holder.outcomeMessage.setText(model.getMessage());
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return messages.size();
    }

    class BotChattingViewHolder extends RecyclerView.ViewHolder
    {
        private TextView incomeResponse,outcomeMessage;
        private ImageView assistantImg;

        BotChattingViewHolder(View itemView)
        {
            super(itemView);

            incomeResponse = itemView.findViewById(R.id.income_response);
            outcomeMessage = itemView.findViewById(R.id.outcome_message);
            assistantImg = itemView.findViewById(R.id.assistant_logo);
        }
    }

    private String formatToYesterdayOrToday(String date) throws ParseException
    {
        Date dateTime = new SimpleDateFormat("d MMM yyyy", Locale.US).parse(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);

        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
        {
            return "Today";
        }
        else if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR))
        {
            return "Yesterday";
        }
        else
        {
            return date;
        }
    }
}
