package com.egoriku.catsrunning.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.egoriku.catsrunning.R;
import com.egoriku.catsrunning.helpers.TypeFit;
import com.egoriku.catsrunning.models.Firebase.SaveModel;
import com.egoriku.catsrunning.utils.ConverterTime;

import static com.egoriku.catsrunning.models.Constants.Color.COLOR_NOW_FIT;
import static com.egoriku.catsrunning.util.DrawableKt.drawableCompat;
import static com.egoriku.catsrunning.util.DrawableKt.drawableTypeFit;

public class FitnessDataHolder extends RecyclerView.ViewHolder {

    private ImageView favoriteImage;
    private ImageView typeFitImage;
    private TextView fitDateText;
    private TextView fitTimeText;
    private CardView cardView;
    private TextView caloriesText;
    private TextView distanceText;
    private Context context;

    private ClickListener clickListener;

    public void setOnClickListener(FitnessDataHolder.ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public FitnessDataHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();

        favoriteImage = (ImageView) itemView.findViewById(R.id.adapter_all_fitness_data_image_liked);
        typeFitImage = (ImageView) itemView.findViewById(R.id.adapter_all_fitness_data_ic_type);
        fitDateText = (TextView) itemView.findViewById(R.id.adapter_all_fitness_data_date_text);
        fitTimeText = (TextView) itemView.findViewById(R.id.adapter_all_fitness_data_time_text_view);
        cardView = (CardView) itemView.findViewById(R.id.adapter_fitness_data_fragment_root_cardview);
        caloriesText = (TextView) itemView.findViewById(R.id.adapter_all_fitness_data_calories_text_view);
        distanceText = (TextView) itemView.findViewById(R.id.adapter_all_fitness_data_distance_text_view);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClickItem(getAdapterPosition());
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clickListener.onLongClick(getAdapterPosition());
                return true;
            }
        });

        favoriteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onFavoriteClick(getAdapterPosition());
            }
        });
    }

    @SuppressLint("StringFormatMatches")
    public void setData(SaveModel data, Context context) {
        distanceText.setText(String.format(context.getString(R.string.adapter_all_fitness_data_distance_meter), data.getDistance()));
        caloriesText.setText(String.format(context.getString(R.string.adapter_all_fitness_data_calories), data.getCalories()));
        fitDateText.setText(ConverterTime.convertUnixDateWithoutHours(data.getBeginsAt()));

        setFitTimeText(data.getBeginsAt(), data.getTime());
        setTypeFitImage(data.getTypeFit());

        favoriteImage.setImageDrawable(data.isFavorite() ?
                drawableCompat(context, R.drawable.ic_vec_star_black) :
                drawableCompat(context, R.drawable.ic_vec_star_border));
    }

    private void setTypeFitImage(@TypeFit int typeFit) {
        typeFitImage.setImageDrawable(drawableTypeFit(context, typeFit));
    }

    private void setFitTimeText(long beginsAt, long time) {
        if (time == 0) {
            fitDateText.setTypeface(null, Typeface.BOLD);
            fitTimeText.setTypeface(null, Typeface.BOLD);
            cardView.setCardBackgroundColor(Color.parseColor(COLOR_NOW_FIT));
        }
        fitTimeText.setText(ConverterTime.ConvertTimeAllFitnessData(beginsAt, time));
    }

    public interface ClickListener {
        void onClickItem(int position);

        void onFavoriteClick(int position);

        void onLongClick(int position);
    }
}
