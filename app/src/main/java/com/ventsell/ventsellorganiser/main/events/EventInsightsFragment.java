package com.ventsell.ventsellorganiser.main.events;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.ventsell.ventsellorganiser.AppController;
import com.ventsell.ventsellorganiser.R;
import com.ventsell.ventsellorganiser.ticket.TicketUiUpdateHelper;
import com.ventsell.ventsellorganiser.ticket.VentsellTicketVerifier;
import com.ventsell.ventsellorganiser.ticket.objects.Input;
import com.ventsell.ventsellorganiser.utils.ClipboardInterface;

import net.sourceforge.zbar.android.CameraTest.TicketScanActivity;

import static android.app.Activity.RESULT_OK;

public class EventInsightsFragment extends Fragment {

    private static final String TAG = EventInsightsFragment.class.getSimpleName();

    public static final int CAMERA_REQUEST_CODE = 0;
    public static final int REQUEST_CODE_SCAN_TICKET = 0;

    private Dialog enterCodeDialog;
    private TicketUiUpdateHelper mTicketUiUpdateHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_insights, container, false);

        final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        final FloatingActionButton fab1 = (FloatingActionButton) view.findViewById(R.id.fab1);
        final FloatingActionButton fab2 = (FloatingActionButton) view.findViewById(R.id.fab2);
        final FloatingActionButton fab3 = (FloatingActionButton) view.findViewById(R.id.fab3);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fab1.isShown() || fab2.isShown() || fab3.isShown()) {
                    fab.setImageResource(R.drawable.ic_unfold_more);
                    fab1.hide();
                    fab2.hide();
                    fab3.hide();
                } else {
                    fab.setImageResource(R.drawable.ic_unfold_less);
                    fab1.show();
                    fab2.show();
                    fab3.show();
                }
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionManualInput();
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionCopyFromClipboard();
            }
        });
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionScan();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter Ticket ID");
        final View customLayout = LayoutInflater.from(getActivity()).inflate(R.layout.view_enter_code_dialog, container, false);
        final EditText enterCodeTextView = (EditText) customLayout.findViewById(R.id.enterCode);
        enterCodeTextView.requestFocus();
        builder.setView(customLayout);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                enterCodeTextView.setText("");
                Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String ticketId = enterCodeTextView.getText().toString();
                if (!TextUtils.isEmpty(ticketId)) {
                    mTicketUiUpdateHelper.verifyAndUpdate(new Input(Input.INPUT_TYPE_ENTERED, ticketId));
                    enterCodeTextView.setText("");
                }
            }
        });
        enterCodeDialog = builder.create();

        View mProgressView = view.findViewById(R.id.progress_view);
        View mChartView = view.findViewById(R.id.insightsMainLayout);
        View mResultView = view.findViewById(R.id.result_view);

        mTicketUiUpdateHelper = new TicketUiUpdateHelper(getActivity(), mProgressView, mChartView, mResultView);

        /*setHasOptionsMenu(true);*/
        return view;
    }

    private void actionScan() {
        startCaptureActivity();
    }

    private void actionCopyFromClipboard() {
        String ticketId = null; // TODO allow multiple copy and paste
        if (ClipboardInterface.hasText(getActivity())) {
            CharSequence text = ClipboardInterface.getText(getActivity());
            ticketId = text != null ? text.toString() : null;
        }
        if (!TextUtils.isEmpty(ticketId)) {
            mTicketUiUpdateHelper.verifyAndUpdate(new Input(Input.INPUT_TYPE_COPY_PASTE, ticketId));
        }
    }

    private void actionManualInput() {
        enterCodeDialog.show();
    }

    private void startCaptureActivity() {
        if (!hasPermissionForCamera()) {
            requestPermissionForCamera();
        } else {
            Intent startIntent = new Intent(getActivity(), TicketScanActivity.class);
            startActivityForResult(startIntent, REQUEST_CODE_SCAN_TICKET);
        }
    }

    private boolean hasPermissionForCamera() {
        int result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionForCamera() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("ventsell", "onRequestPermissionsResult");
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (permissions[0].equals(Manifest.permission.CAMERA) && !(grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Snackbar.make(getActivity().findViewById(R.id.insights_coordinator_view), "Camera Permission needed. Please allow in App Settings for additional functionality.", Snackbar.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(getActivity(), TicketScanActivity.class);
                Log.d("ventsell", "OK");
                startActivity(intent);
            }
        }
    }

    public boolean onBackPressed() {
        return mTicketUiUpdateHelper.resetViews();
    }

    @Override
    public void onStop() {
        super.onStop();
        AppController.getInstance().cancelPendingRequests(VentsellTicketVerifier.TAG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCAN_TICKET){
            if (resultCode == RESULT_OK){
                String ticketId = data.getStringExtra(TicketScanActivity.EXTRA_SCAN_RESULT);
                if (!TextUtils.isEmpty(ticketId)) {
                    mTicketUiUpdateHelper.verifyAndUpdate(new Input(Input.INPUT_TYPE_SCANNED, ticketId));
                }
            }
        }
    }

    //--------------------------------------------------------------------------------------

    /**
     * Custom implementation of the MarkerView.
     *
     * @author Philipp Jahoda
     */
    public static class MyMarkerView extends MarkerView {

        private TextView tvContent;

        public MyMarkerView(Context context) {
            super(context, 0);
        }

        public MyMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);

            tvContent = (TextView) findViewById(R.id.tvContent);
        }

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {

            tvContent.setText(String.format("%s", Utils.formatNumber(e.getY(), 0, true)));

            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2), -getHeight());
        }
    }
}
