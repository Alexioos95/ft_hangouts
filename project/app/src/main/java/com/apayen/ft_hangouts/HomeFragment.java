package com.apayen.ft_hangouts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.util.Comparator;
import java.util.List;

public class HomeFragment extends Fragment
{
    //////////////////////////////
    // Variables
    //////////////////////////////
    DatabaseManager db;
    private BroadcastReceiver receiver;
    //////////////////////////////
    // Creation
    //////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState)
    { super.onCreate(savedInstanceState); }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    { return (inflater.inflate(R.layout.fragment_home, container, false)); }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        // Init variables
        db = new DatabaseManager(requireContext());
        // Load saved contacts
        List<ContactModel> savedContacts = db.getContacts();
        savedContacts.sort(Comparator.comparing(ContactModel::getName));
        LinearLayout listWrapper = view.findViewById(R.id.contact_list_wrapper);
        for (ContactModel contact : savedContacts)
        {
            // LinearLayout - Wrapper
            int paddingPx = Utils.convertDpToPx(requireContext(), 8);
            LinearLayout wrapper = new LinearLayout(requireContext());
            LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            wrapperParams.bottomMargin = paddingPx;

            wrapper.setBackgroundColor(Color.TRANSPARENT);
            wrapper.setLayoutParams(wrapperParams);
            wrapper.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
            wrapper.setOrientation(LinearLayout.HORIZONTAL);
            wrapper.setGravity(Gravity.CENTER_VERTICAL);
            wrapper.setElevation(6f);
            wrapper.setTranslationZ(2f);
            wrapper.setClickable(true);
            wrapper.setFocusable(true);
            try
            {
                int[] attrs = new int[]{android.R.attr.selectableItemBackgroundBorderless};
                TypedArray a = requireContext().obtainStyledAttributes(attrs);
                int drawableId = a.getResourceId(0, 0);

                a.recycle();
                wrapper.setForeground(ContextCompat.getDrawable(requireContext(), drawableId));
            }
            catch (Exception e) { System.out.println("ERROR"); }
            // LinearLayout - EventListener
            wrapper.setOnClickListener(v -> wrapper.postDelayed(() -> {
				Fragment contactFragment = new ContactFragment();
				Bundle bundle = new Bundle();

				bundle.putParcelable("data", contact);
				contactFragment.setArguments(bundle);
				FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
				transaction.setCustomAnimations(R.anim.anim_enter_slide_right, R.anim.anim_exit_slide_right, R.anim.anim_pop_slide_right, R.anim.anim_popexit_slide_right);
				transaction.replace(R.id.fragment_container, contactFragment);
				transaction.addToBackStack(null);
				transaction.commit();
			}, 70));
            // ImageView inside wrapper - Photo
            ImageView imageView = new ImageView(requireContext());
            LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(Utils.convertDpToPx(requireContext(), 40), Utils.convertDpToPx(requireContext(), 40));

            if (contact.getPhoto() != null)
            {
                File imgFile = new File(contact.getPhoto());
                if (imgFile.exists())
                {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imageView.setImageBitmap(bitmap);
                }
            }
            else
                imageView.setImageResource(R.drawable.default_photo);
            imageView.setClipToOutline(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.shape_circle));
            viewParams.setMargins(0, 0, Utils.convertDpToPx(requireContext(), 15), 0);
            imageView.setLayoutParams(viewParams);
            // TextView inside wrapper - Name
            TextView name = new TextView(requireContext());
            ViewGroup.LayoutParams textParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            name.setLayoutParams(textParams);
            name.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            name.setText(contact.getName());
            // Merge
            wrapper.addView(imageView);
            wrapper.addView(name);
            wrapper.setBackgroundColor(Color.WHITE);
            listWrapper.addView(wrapper);
        }
        // Event Listener to add Contact
        FloatingActionButton fab = view.findViewById(R.id.add_contact_FAB);
        fab.setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_enter_slide_right, R.anim.anim_exit_slide_right, R.anim.anim_pop_slide_right, R.anim.anim_popexit_slide_right)
                .replace(R.id.fragment_container, new ContactFragment())
                .addToBackStack("")
                .commit());
    }
    //////////////////////////////
    // Start and Stop
    //////////////////////////////
    @Override
    public void onStart()
    {
        super.onStart();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String sender = intent.getStringExtra("sender");
                String message = intent.getStringExtra("message");

                if (sender != null && message != null)
                {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment())
                            .commit();
                }
            }
        };
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, new IntentFilter("com.apayen.NEW_SMS"));
    }
    @Override
    public void onStop()
    {
        super.onStop();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver);
    }
}