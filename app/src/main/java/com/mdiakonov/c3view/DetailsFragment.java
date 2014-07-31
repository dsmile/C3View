package com.mdiakonov.c3view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class DetailsFragment extends Fragment {

    TextView txtListName;
    TextView txtListAge;
    TextView txtListBirthday;
    TextView txtListSpeciality;
    ImageView imgAvatar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_details, null);

        txtListAge = (TextView) view.findViewById(R.id.lblDetailsAge);
        txtListBirthday = (TextView) view.findViewById(R.id.lblDetailsBirthday);
        txtListName = (TextView) view.findViewById(R.id.lblDetailsName);
        txtListSpeciality = (TextView) view.findViewById(R.id.lblDetailsSpeciality);
        imgAvatar = (ImageView) view.findViewById(R.id.detailsAvatar);

        updateDetail(getArguments());

        return view;
    }

    public void updateDetail(Bundle bundle) {
        if (bundle != null) {
            String birthday = bundle.getString("birthday");
            txtListName.setText(bundle.getString("name"));
            txtListSpeciality.setText(bundle.getString("specs"));
            txtListBirthday.setText(birthday);
            txtListAge.setText(DataConditioning.properAgeLabel(DataConditioning.properAge(birthday)));
            UrlImageViewHelper.setUrlDrawable(imgAvatar, bundle.getString("avatar_url"), R.drawable.no_avatar);
        }
    }

}