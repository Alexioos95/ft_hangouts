package com.apayen.ft_hangouts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.List;
import java.util.Objects;

public class SMSFragment extends Fragment
{
	//////////////////////////////
	// Variables
	//////////////////////////////
	private BroadcastReceiver receiver;
	AppCompatActivity activity;
	DatabaseManager db;
	int heightScreen;
	ContactModel contactData;
	ScrollView scrollView;
	LinearLayout listWrapper;
	LinearLayout footer;
	EditText footer_et;
	ImageView footer_send;
	//////////////////////////////
	// Creation
	//////////////////////////////
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		// Change actions
		menu.clear();
		inflater.inflate(R.menu.menu_message_actionbar, menu);
		// Set action's color
		SharedPreferences preferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
		Utils.updateHeaderColor(activity, preferences, Utils.getColorFromStorage(requireContext()));
	}
	public void onPrepareOptionsMenu(@NonNull Menu menu)
	{ super.onPrepareOptionsMenu(menu); }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_s_m_s, container, false);
		// Init variables
		activity = (AppCompatActivity)requireActivity();
		db = new DatabaseManager(requireContext());
		heightScreen = getResources().getDisplayMetrics().heightPixels;
		scrollView = view.findViewById(R.id.sms_list_scrollview);
		listWrapper = view.findViewById(R.id.sms_list_wrapper);
		footer = view.findViewById(R.id.sms_footer);
		footer_et = view.findViewById(R.id.sms_footer_et);
		footer_send = view.findViewById(R.id.sms_footer_send);
		// Set contact's data
		Bundle arguments = getArguments();

		if (arguments != null)
		{
			contactData = arguments.getParcelable("contactData");
			ImageView photo = view.findViewById(R.id.sms_header_photo);
			TextView name = view.findViewById(R.id.sms_header_name);
			TextView phone = view.findViewById(R.id.sms_header_phone);

			if (contactData.getPhoto() != null)
			{
				File imgFile = new File(contactData.getPhoto());
				if (imgFile.exists())
				{
					Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
					photo.setImageBitmap(bitmap);
				}
			}
			else
				photo.setImageResource(R.drawable.default_photo);
			photo.setClipToOutline(true);
			photo.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.shape_circle));
			name.setText(contactData.getName());
			phone.setText(contactData.getPhone());
		}
		else
		{
			getParentFragmentManager().popBackStack();
			Toast.makeText(getContext(), getString(R.string.error_loading_data), Toast.LENGTH_SHORT).show();
		}
		// Add the SMS history
		List<SMSModel> history = db.getSMSs(contactData.getID());
		for (SMSModel sms : history)
		{
			int paddingPx = Utils.convertDpToPx(requireContext(), 12);
			// LinearLayout - Wrapper
			LinearLayout wrapper = new LinearLayout(requireContext());
			LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			wrapperParams.bottomMargin = paddingPx;

			if (Objects.equals(sms.getType(), "Receive"))
				wrapperParams.gravity = Gravity.START;
			else
				wrapperParams.gravity = Gravity.END;
			wrapper.setLayoutParams(wrapperParams);
			// TextView - Message
			TextView message = new TextView(requireContext());
			LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			textParams.setMargins(0, 0, 0, 0);
			message.setLayoutParams(textParams);
			message.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
			message.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
			message.setText(sms.getMessage());
			message.setTextSize(16);
			message.setMaxWidth(Utils.convertDpToPx(requireContext()	, 250));
			if (Objects.equals(sms.getType(), "Receive"))
				message.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.shape_round_rectangle_left));
			else
				message.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.shape_round_rectangle_right));
			// ImageView - Tail of message's bubble
			ImageView tail = new ImageView(requireContext());

			LinearLayout.LayoutParams tailParams = new LinearLayout.LayoutParams(Utils.convertDpToPx(requireContext(), 14), Utils.convertDpToPx(requireContext(), 13));
			tailParams.gravity = Gravity.BOTTOM;

			if (Objects.equals(sms.getType(), "Receive"))
			{
				tail.setImageResource(R.drawable.bubble_arrow_left);
				tail.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white));
			}
			else
			{
				tail.setImageResource(R.drawable.bubble_arrow_right);
				tail.setColorFilter(ContextCompat.getColor(requireContext(), R.color.lightBlue));
			}
			tailParams.setMargins(0, 0, 0, 0);
			tail.setLayoutParams(tailParams);
			// Merge
			if (Objects.equals(sms.getType(), "Receive"))
			{
				wrapper.addView(tail);
				wrapper.addView(message);
			}
			else
			{
				wrapper.addView(message);
				wrapper.addView(tail);
			}
			listWrapper.addView(wrapper);
		}
		scrollView.post(() -> scrollView.smoothScrollTo(0, listWrapper.getHeight()));
		// Event Listener to disable keyboard
		view.findViewById(R.id.sms_wrapper).setOnTouchListener((v, event) -> {
			if (event.getAction() == MotionEvent.ACTION_UP)
			{
				InputMethodManager imm = (InputMethodManager)requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

				if (imm != null)
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				v.performClick();
			}
			return (true);
		});
		view.findViewById(R.id.sms_list_scrollview).setOnTouchListener((v, event) -> {
			if (event.getAction() == MotionEvent.ACTION_UP)
			{
				InputMethodManager imm = (InputMethodManager)requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

				if (imm != null)
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				v.performClick();
			}
			return (false);
		});
		// Event listener to send SMS
		footer_send.setOnClickListener(v -> {
			String message = footer_et.getText().toString();

			if (!message.isEmpty())
			{
				SMSModel smsModel = new SMSModel(-1, contactData.getID(), "Send", message, System.currentTimeMillis());
				SMSManager.send(requireContext(), contactData.getPhone(), message);
				footer_et.setText("");
				if (SMSManager.getErrno() != 0)
					alertErrorSendSMS(db, smsModel);
				else if (!db.addSMS(smsModel))
					alertErrorAddSMS(db, smsModel);
				else
				{
					int paddingPx = Utils.convertDpToPx(requireContext(), 12);
					// LinearLayout - Wrapper
					LinearLayout wrapper = new LinearLayout(requireContext());
					LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					wrapperParams.bottomMargin = paddingPx;
					wrapperParams.gravity = Gravity.END;

					wrapper.setLayoutParams(wrapperParams);
					// TextView - Message
					TextView msg = new TextView(requireContext());
					LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

					textParams.setMargins(0, 0, 0, 0);
					msg.setLayoutParams(textParams);
					msg.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
					msg.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
					msg.setText(smsModel.getMessage());
					msg.setTextSize(16);
					msg.setMaxWidth(Utils.convertDpToPx(requireContext()	, 250));
					msg.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.shape_round_rectangle_right));
					// ImageView - Tail of message's bubble
					ImageView tail = new ImageView(requireContext());

					LinearLayout.LayoutParams tailParams = new LinearLayout.LayoutParams(Utils.convertDpToPx(requireContext(), 14), Utils.convertDpToPx(requireContext(), 13));
					tailParams.gravity = Gravity.BOTTOM;
					tail.setImageResource(R.drawable.bubble_arrow_right);
					tail.setColorFilter(ContextCompat.getColor(requireContext(), R.color.lightBlue));
					tailParams.setMargins(0, 0, 0, 0);
					tail.setLayoutParams(tailParams);
					// Merge
					wrapper.addView(msg);
					wrapper.addView(tail);
					listWrapper.addView(wrapper);
					InputMethodManager imm = (InputMethodManager)requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

					if (imm != null)
						imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					v.performClick();
					scrollView.post(() -> scrollView.smoothScrollTo(0, listWrapper.getHeight()));
				}
			}
		});
		// Change Send icon color depending on if string empty
		footer_et.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) { }
			@Override
			public void onTextChanged(CharSequence charSequence, int start, int before, int count)
			{
				if (charSequence.toString().isEmpty())
					footer_send.setColorFilter(ContextCompat.getColor(requireContext(), R.color.lightGray));
				else
					footer_send.setColorFilter(ContextCompat.getColor(requireContext(), R.color.darkGray));
			}
			@Override
			public void afterTextChanged(Editable editable) { }
		});
		// Resize Footer when keyboard is active
		view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
			Rect rect = new Rect();
			view.getWindowVisibleDisplayFrame(rect);
			int screenHeight = view.getRootView().getHeight();
			int keypadHeight = screenHeight - rect.bottom;

			if (keypadHeight > screenHeight * 0.15)
				footer.getLayoutParams().height = keypadHeight - 80;
			else
				footer.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
			footer.requestLayout();
		});
		return (view);
	}
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
	{ super.onViewCreated(view, savedInstanceState); }
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

				if (sender == null || message == null || !Objects.equals(contactData.getPhone(), sender))
					return ;
				int paddingPx = Utils.convertDpToPx(requireContext(), 12);
				// LinearLayout - Wrapper
				LinearLayout wrapper = new LinearLayout(requireContext());
				LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				wrapperParams.bottomMargin = paddingPx;
				wrapperParams.gravity = Gravity.START;
				wrapper.setLayoutParams(wrapperParams);
				// TextView - Message
				TextView msg = new TextView(requireContext());
				LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

				textParams.setMargins(0, 0, 0, 0);
				msg.setLayoutParams(textParams);
				msg.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
				msg.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
				msg.setText(message);
				msg.setTextSize(16);
				msg.setMaxWidth(Utils.convertDpToPx(requireContext()	, 250));
				msg.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.shape_round_rectangle_left));
				// ImageView - Tail of message's bubble
				ImageView tail = new ImageView(requireContext());
				LinearLayout.LayoutParams tailParams = new LinearLayout.LayoutParams(Utils.convertDpToPx(requireContext(), 14), Utils.convertDpToPx(requireContext(), 13));
				tailParams.gravity = Gravity.BOTTOM;

				tail.setImageResource(R.drawable.bubble_arrow_left);
				tail.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white));
				tailParams.setMargins(0, 0, 0, 0);
				tail.setLayoutParams(tailParams);
				// Merge
				wrapper.addView(tail);
				wrapper.addView(msg);
				listWrapper.addView(wrapper);
				scrollView.post(() -> scrollView.smoothScrollTo(0, listWrapper.getHeight()));
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
	//////////////////////////////
	// Action bar's menu
	//////////////////////////////
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_call_contact)
		{
			Intent intent = new Intent(Intent.ACTION_DIAL);

			intent.setData(Uri.parse("tel:" + contactData.getPhone()));
			startActivity(intent);
		}
		else if (item.getItemId() == R.id.action_delete_sms_history)
		{
			new AlertDialog.Builder(requireContext())
					.setTitle(getString(R.string.sure))
					.setMessage(getString(R.string.ask_delete_sms) + "\n")
					.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
						if (db.deleteSMS(contactData.getID()))
							Toast.makeText(getContext(), getString(R.string.error_deleting_sms_history), Toast.LENGTH_SHORT).show();
						else
							getParentFragmentManager().popBackStack();
					})
					.setNegativeButton(getString(R.string.error_cancel), (dialog, which) -> { })
					.show();
		}
		return (true);
	}
	//////////////////////////////
	// Alerts
	//////////////////////////////
	private void alertErrorSendSMS(DatabaseManager db, SMSModel sms)
	{
		new AlertDialog.Builder(requireContext())
				.setTitle(getString(R.string.error))
				.setMessage(getString(R.string.error_sending_sms) + "\n" + SMSManager.getError(requireContext(), SMSManager.getErrno()))
				.setPositiveButton(getString(R.string.error_retry), (dialog, which) -> {
					SMSManager.send(requireContext(), contactData.getPhone(), sms.getMessage());
					if (SMSManager.getErrno() != 0)
						alertErrorSendSMS(db, sms);
					else if (!db.addSMS(sms))
						alertErrorAddSMS(db, sms);
				})
				.setNegativeButton(getString(R.string.error_cancel), (dialog, which) -> { })
				.show();
	}
	private void alertErrorAddSMS(DatabaseManager db, SMSModel sms)
	{
		new AlertDialog.Builder(requireContext())
				.setTitle(getString(R.string.error))
				.setMessage(getString(R.string.error_adding_sms))
				.setPositiveButton(getString(R.string.error_retry), (dialog, which) -> {
					if (!db.addSMS(sms))
						alertErrorAddSMS(db, sms);
				})
				.setNegativeButton(getString(R.string.error_cancel), (dialog, which) -> { })
				.show();
	}
}