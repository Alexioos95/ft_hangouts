package com.apayen.ft_hangouts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.activity.result.contract.ActivityResultContracts;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.EditText;
import android.widget.ScrollView;
import android.view.inputmethod.InputMethodManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ContactFragment extends Fragment implements Utils.PermissionResultCallback
{
    //////////////////////////////
    // Variables
    //////////////////////////////
    AppCompatActivity activity;
    DatabaseManager db;
    private boolean isSaved = false;
    private boolean isEditing = false;
    private int heightScreen;
	private ScrollView scrollView;
    private View wrapper;
    private ImageView photoView;
    private FloatingActionButton fab;
    private ContactModel data;
    private Uri uri_photo = null;
    private EditText et_name, et_phone, et_email, et_street, et_city, et_zip, et_notes;
	private String str_name, str_phone;
    //////////////////////////////
    // Creation
    //////////////////////////////
    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                if (!isEditing)
                {
                    setEnabled(false);
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
                else
                {
                    if (activity != null)
                        activity.invalidateOptionsMenu();
                    // Restore contact's data
                    if (data.getPhoto() != null)
                    {
                        File imgFile = new File(data.getPhoto());
                        if (imgFile.exists())
                        {
                            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            photoView.setImageBitmap(bitmap);
                        }
                    }
                    else
                        photoView.setImageResource(R.drawable.default_photo);
                    et_name.setText(data.getName());
                    et_phone.setText(data.getPhone());
                    et_email.setText(data.getEmail());
                    et_street.setText(data.getStreet());
                    et_city.setText(data.getCity());
                    et_zip.setText(data.getZip());
                    et_notes.setText(data.getNotes());
                    // Restore design and bool
                    disableEditTexts();
                    fab.hide();
                    isEditing = false;
                }
            }
        });
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isEditing", isEditing);
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        // Change actions
        menu.clear();
        inflater.inflate(R.menu.menu_contact_actionbar, menu);
        // Set action's color
        SharedPreferences preferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        Utils.updateHeaderColor(activity, preferences, Utils.getColorFromStorage(requireContext()));
    }
    public void onPrepareOptionsMenu(@NonNull Menu menu)
    {
        if (activity != null)
        {
            Toolbar toolbar = activity.findViewById(R.id.my_toolbar);
			MenuItem phoneActionItem = toolbar.getMenu().findItem(R.id.action_call_contact);
			MenuItem messageActionItem = toolbar.getMenu().findItem(R.id.action_message_contact);
			MenuItem editActionItem = toolbar.getMenu().findItem(R.id.action_edit_contact);
            MenuItem saveActionItem = toolbar.getMenu().findItem(R.id.action_save_contact);
            MenuItem deleteActionItem = toolbar.getMenu().findItem(R.id.action_delete_contact);

            if (isSaved && !isEditing)
            {
                if (saveActionItem != null)
                    saveActionItem.setVisible(false);
            }
            else
            {
                if (phoneActionItem != null)
                    phoneActionItem.setVisible(false);
                if (messageActionItem != null)
                    messageActionItem.setVisible(false);
                if (editActionItem != null)
                    editActionItem.setVisible(false);
                if (deleteActionItem != null)
                    deleteActionItem.setVisible(false);
            }
        }
        super.onPrepareOptionsMenu(menu);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        // Init variables
        activity = (AppCompatActivity)requireActivity();
        db = new DatabaseManager(requireContext());
        fab = view.findViewById(R.id.contact_change_photo);
        et_name = view.findViewById(R.id.contact_input_name);
        et_phone = view.findViewById(R.id.contact_input_phone);
        et_email = view.findViewById(R.id.contact_input_email);
        et_street = view.findViewById(R.id.contact_input_street);
        et_city = view.findViewById(R.id.contact_input_city);
        et_zip = view.findViewById(R.id.contact_input_zip);
        et_notes = view.findViewById(R.id.contact_input_notes);
        wrapper = view.findViewById(R.id.contact_frag_wrapper);
        photoView = view.findViewById(R.id.contact_photo);
        scrollView = view.findViewById(R.id.contact_scrollview);
        heightScreen = getResources().getDisplayMetrics().heightPixels;
        // Adapt design depending on if the contact is already saved
        Bundle arguments = getArguments();

        if (savedInstanceState != null)
            isEditing = savedInstanceState.getBoolean("isEditing");
        if (arguments != null)
        {
            data = arguments.getParcelable("data");
            isSaved = true;

            if (data != null)
            {
                if (data.getPhoto() != null)
                {
                    File imgFile = new File(data.getPhoto());
                    if (imgFile.exists())
                    {
                        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        photoView.setImageBitmap(bitmap);
                    }
                }
                else
                    photoView.setImageResource(R.drawable.default_photo);
                et_name.setText(data.getName());
                et_phone.setText(data.getPhone());
                et_email.setText(data.getEmail());
                et_street.setText(data.getStreet());
                et_city.setText(data.getCity());
                et_zip.setText(data.getZip());
                et_notes.setText(data.getNotes());
                disableEditTexts();
            }
        }
        if (activity != null)
            activity.invalidateOptionsMenu();
        // Event Listener to disable keyboard
        view.findViewById(R.id.contact_wrapper).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP)
            {
                InputMethodManager imm = (InputMethodManager)requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null)
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                v.performClick();
            }
            return (true);
        });
        // Resize ScrollView when keyboard is active
        view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect rect = new Rect();
            view.getWindowVisibleDisplayFrame(rect);
            int screenHeight = view.getRootView().getHeight();
            int keypadHeight = screenHeight - rect.bottom;

            if (keypadHeight > screenHeight * 0.15)
                wrapper.getLayoutParams().height = heightScreen - keypadHeight - 50;
            else
                wrapper.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            scrollView.requestLayout();
        });
        return (view);
    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        // Event Listener to change photo
        final ActivityResultLauncher<String> getContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null)
            {
                uri_photo = result;
                photoView.setImageURI(result);
            }
            else
                Toast.makeText(getContext(), getString(R.string.contact_no_photo), Toast.LENGTH_SHORT).show();
        });
        fab.setOnClickListener(v -> getContent.launch("image/*"));
        if (isSaved && !isEditing)
            fab.hide();
        if (isEditing)
            enableEditTexts();
    }
    //////////////////////////////
    // Action bar's menu
    //////////////////////////////
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_call_contact)
        {
            Intent intent = new Intent(Intent.ACTION_DIAL);

            intent.setData(Uri.parse("tel:" + data.getPhone()));
            startActivity(intent);
        }
        else if (item.getItemId() == R.id.action_message_contact)
            Utils.requestSMSPermissions(getActivity(), this);
        else if (item.getItemId() == R.id.action_save_contact)
        {
            // Setup variables
			String str_photo = savePhoto(uri_photo);
            str_name = et_name.getText().toString();
            str_phone = et_phone.getText().toString();
			String str_email = et_email.getText().toString();
			String str_street = et_street.getText().toString();
			String str_city = et_city.getText().toString();
			String str_zip = et_zip.getText().toString();
			String str_notes = et_notes.getText().toString();
            // Check form
            if (!formIsValid())
                return (true);
            // Set caps
            str_name = str_name.substring(0, 1).toUpperCase() + str_name.substring(1);
            // Push to DB
            DatabaseManager db = new DatabaseManager(requireContext());
            ContactModel contact;
            if (str_photo == null && isEditing)
                str_photo = data.getPhoto();
            contact = new ContactModel((isSaved ? data.getID() : -1), str_photo, str_name, str_phone, str_email, str_street, str_city, str_zip, str_notes);
            if (!isSaved)
            {
                if (!db.addContact(contact))
                    alertErrorAddContact(db, contact);
                else
                {
                    requireActivity().getSupportFragmentManager().popBackStack();
                    Toast.makeText(getContext(), getString(R.string.saved_contact), Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                if (!db.updateContact(contact) && data.equals(contact))
                    alertErrorEditContact(db, contact);
                else
                {
                    data = contact;

                    if (activity != null)
                        activity.invalidateOptionsMenu();
                    fab.hide();
                    disableEditTexts();
                    Toast.makeText(getContext(), getString(R.string.edited_contact), Toast.LENGTH_SHORT).show();
                    isEditing = false;
                }
            }
            return (true);
        }
        else if (item.getItemId() == R.id.action_edit_contact)
        {
            isEditing = true;

            if (activity != null)
                activity.invalidateOptionsMenu();
            fab.show();
            enableEditTexts();
        }
        else if (item.getItemId() == R.id.action_delete_contact)
        {
            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.sure))
                    .setMessage(getString(R.string.ask_delete_contact))
                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                        db.deleteContact(data);
                        Toast.makeText(getContext(), getString(R.string.deleted_contact), Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    })
                    .setNegativeButton(getString(R.string.no), (dialog, which) -> {})
                    .show();
        }
        return (super.onOptionsItemSelected(item));
    }
    private String savePhoto(Uri photo)
    {
        try
        {
            if (photo == null)
                return (null);
            InputStream inputStream = requireActivity().getContentResolver().openInputStream(photo);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            File directory = requireActivity().getFilesDir();
            File imageFile = new File(directory, "uploaded_photo_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(imageFile);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            return (imageFile.getAbsolutePath());
        }
        catch (Exception e)
        { Toast.makeText(getContext(), getString(R.string.error_saving_photo), Toast.LENGTH_SHORT).show(); }
        return (null);
    }
    private boolean formIsValid()
    {
        boolean isValid = true;

        if (str_name.isEmpty())
        {
            isValid = false;
            et_name.setError(getString(R.string.error_required_name));
        }
        if (str_phone.isEmpty())
        {
            isValid = false;
            et_phone.setError(getString(R.string.error_required_phone));
        }
        return (isValid);
    }
    private void alertErrorAddContact(DatabaseManager db, ContactModel contact)
    {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.error))
                .setMessage(getString(R.string.error_adding_contact))
                .setPositiveButton(getString(R.string.error_retry), (dialog, which) -> {
                    if (!db.addContact(contact))
                        alertErrorAddContact(db, contact);
                    else
                        Toast.makeText(getContext(), getString(R.string.saved_contact), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.error_cancel), (dialog, which) -> requireActivity().getSupportFragmentManager().popBackStack())
                .show();
    }
    private void alertErrorEditContact(DatabaseManager db, ContactModel contact)
    {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.error))
                .setMessage(getString(R.string.error_editing_contact))
                .setPositiveButton(getString(R.string.error_retry), (dialog, which) -> {
                    if (!db.updateContact(contact) && data.equals(contact))
                        alertErrorEditContact(db, contact);
                    else
                        Toast.makeText(getContext(), getString(R.string.edited_contact), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.error_cancel), (dialog, which) -> requireActivity().getSupportFragmentManager().popBackStack())
                .show();
    }
    //////////////////////////////
    // Edit Texts
    //////////////////////////////
    private void disableEditTexts()
    {
        et_name.setInputType(0);
        et_phone.setInputType(0);
        et_email.setInputType(0);
        et_street.setInputType(0);
        et_city.setInputType(0);
        et_zip.setInputType(0);
        et_notes.setInputType(0);
    }
    private void enableEditTexts()
    {
        et_name.setInputType(1);
        et_phone.setInputType(3);
        et_email.setInputType(1);
        et_street.setInputType(1);
        et_city.setInputType(1);
        et_zip.setInputType(1);
        et_notes.setInputType(1);
    }
    //////////////////////////////
    // Permissions
    //////////////////////////////
    @Override
    public void onPermissionGranted()
    {
        Fragment smsFragment = new SMSFragment();
        Bundle bundle = new Bundle();

        bundle.putParcelable("contactData", data);
        smsFragment.setArguments(bundle);
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.anim_enter_slide_right, R.anim.anim_exit_slide_right, R.anim.anim_pop_slide_right, R.anim.anim_popexit_slide_right);
        transaction.replace(R.id.fragment_container, smsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    @Override
    public void onPermissionDenied()
    { Toast.makeText(requireContext(), getString(R.string.sms_permissions_required), Toast.LENGTH_SHORT).show(); }
}