package com.ventsell.ventsellorganiser.ticket;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ventsell.ventsellorganiser.R;
import com.ventsell.ventsellorganiser.main.history.HistoryManager;
import com.ventsell.ventsellorganiser.main.history.database.objects.HistoryItem;
import com.ventsell.ventsellorganiser.ticket.objects.Result;
import com.ventsell.ventsellorganiser.utils.Constants;

public class ScanResultFragment extends Fragment {

    private static final String HISTORY_ITEM_NUMBER = "history_item_number";
    private static final String TAG = ScanResultFragment.class.getSimpleName();

    private AppCompatImageView mVerifyImageView;
    private AppCompatTextView mVerifyTextView;

    public static ScanResultFragment newInstance(int param1) {
        ScanResultFragment fragment = new ScanResultFragment();
        Bundle args = new Bundle();
        args.putInt(HISTORY_ITEM_NUMBER, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            HistoryManager mHistoryManager = new HistoryManager(getActivity());
            mHistoryManager.trimHistory();

            int itemNumber = getArguments().getInt(Constants.History.ITEM_NUMBER, -1);
            if (itemNumber >= 0) {
                HistoryItem historyItem = mHistoryManager.buildHistoryItem(itemNumber);
                Result result = historyItem.getResult();

                updateUi(result.resultType == Result.RESULT_TYPE_VALID);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_scan_result, container, false);

        mVerifyImageView = (AppCompatImageView) view.findViewById(R.id.verify_imageview);
        mVerifyTextView = (AppCompatTextView) view.findViewById(R.id.verify_textview);

        return view;
    }

    private void updateUi(final boolean isVerified) {
        Log.d("ventsell", TAG + " updateUi = [verified: " + isVerified + "]");

        mVerifyImageView.setImageResource(isVerified ? R.drawable.img_valid_ticket : R.drawable.img_invalid_ticket);
        mVerifyTextView.setText(getContext().getResources().getText(isVerified ? R.string.checked_out_text : R.string.invalid_ticket_text));
        mVerifyTextView.setBackgroundColor(ContextCompat.getColor(getContext(), isVerified ? R.color.colorPrimary : R.color.invalid_ticket));
    }
}
