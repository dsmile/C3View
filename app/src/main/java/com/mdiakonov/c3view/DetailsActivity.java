package com.mdiakonov.c3view;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class DetailsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        TextView txtListAge = (TextView) findViewById(R.id.lblDetailsAge);
        TextView txtListBirthday = (TextView) findViewById(R.id.lblDetailsBirthday);
        TextView txtListName = (TextView) findViewById(R.id.lblDetailsName);
        TextView txtListSpeciality = (TextView) findViewById(R.id.lblDetailsSpeciality);
        ImageView imgAvatar = (ImageView) findViewById(R.id.detailsAvatar);

        Intent intent = getIntent();

        Bundle bundle = intent.getExtras();
        String birthday = bundle.getString("birthday");
        txtListName.setText(bundle.getString("name"));
        txtListSpeciality.setText(bundle.getString("specs"));
        txtListBirthday.setText(birthday);
        txtListAge.setText(DataConditioning.properAgeLabel(DataConditioning.properAge(birthday)));
        UrlImageViewHelper.setUrlDrawable(imgAvatar, bundle.getString("avatar_url"), R.drawable.no_avatar);
    }

}
