package com.peevs.dictpick.logic;

import android.os.AsyncTask;
import android.widget.ImageView;
import com.peevs.dictpick.R;

/**
 * Created by zarrro on 13.2.2016 г..
 */
public class DictOnSaveTranslation extends PostSaveTranslation {
    @Override
    public void onPostExecute(AsyncTask<?, ?, Long> task, Long result) {
        super.onPostExecute(task, result);

        ImageView star = null;
        if (result != null && result != -1 && saveTask.getLastClickedView() != null
                && (star = (ImageView) saveTask.getLastClickedView().findViewById(
                R.id.translation_list_item_star)) != null) {
            star.setImageResource(R.drawable.ic_star_enabled);
        }
        saveTask.getLastClickedView().setOnClickListener(null);
    }
}
