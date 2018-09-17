package com.ventsell.ventsellorganiser.ticket;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ventsell.ventsellorganiser.R;
import com.ventsell.ventsellorganiser.main.history.HistoryManager;
import com.ventsell.ventsellorganiser.ticket.objects.Input;
import com.ventsell.ventsellorganiser.ticket.objects.Result;
import com.ventsell.ventsellorganiser.ticket.objects.VentsellETicketObject;

import static com.ventsell.ventsellorganiser.ticket.VentsellTicketVerifier.ERROR_FAILED_TO_VERIFY_TICKET;
import static com.ventsell.ventsellorganiser.ticket.VentsellTicketVerifier.ERROR_INVALID_QR_CODE;
import static com.ventsell.ventsellorganiser.ticket.VentsellTicketVerifier.TicketVerifierResponseCallBack;

public class TicketUiUpdateHelper {

    private static final String TAG = TicketUiUpdateHelper.class.getSimpleName();

    private final View mProgressView;
    private final View mMainView;
    private final View mResultView;
    private AppCompatImageView mVerifyImageView;
    private AppCompatTextView mVerifyTextView;

    private Context context;

    private HistoryManager mHistoryManager;

    private Input mInput;
    private Result mOutput;

    private VentsellTicketVerifier ventsellTicketVerifier = new VentsellTicketVerifier();
    private TicketVerifierResponseCallBack ticketVerifierResponseCallBack = new TicketVerifierResponseCallBack() {
        @Override
        public void successCallBack(VentsellETicketObject result) {
            Log.d("ventsell", TAG + "> Current e-ticket: " + result);
            if (mHistoryManager != null) {
                mHistoryManager.addHistoryItem(new Result(mInput, Result.RESULT_TYPE_VALID));
            }
            showResult(true);
        }

        @Override
        public void errorCallBack(String error) {
            Log.d("ventsell", TAG + "> Error verifying qr-code [" + error + "]");

            switch (error) {
                case ERROR_FAILED_TO_VERIFY_TICKET:
                    showMain();
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_INVALID_QR_CODE:
                    if (mHistoryManager != null) {
                        mHistoryManager.addHistoryItem(new Result(mInput, Result.RESULT_TYPE_NOT_VALID));
                    }
                    showResult(false);
                    break;
                default:
                    showMain();
            }
        }
    };

    public TicketUiUpdateHelper(Activity activity, View progress, View main, View result) {
        this.context = activity;
        this.mProgressView = progress;
        this.mMainView = main;
        this.mResultView = result;
        mVerifyImageView = (AppCompatImageView) result.findViewById(R.id.verify_imageview);
        mVerifyTextView = (AppCompatTextView) result.findViewById(R.id.verify_textview);

        mHistoryManager = new HistoryManager(activity);
        mHistoryManager.trimHistory();

        mResultView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetViews();
            }
        });
    }

    public void verifyAndUpdate(Input input) {
        showProgress();
        mInput = input;
        ventsellTicketVerifier.verifyTicket(mInput.ticketId, ticketVerifierResponseCallBack);
    }

    private void showProgress() {
        updateUi(true, false, false, false);
    }

    private void showMain() {
        updateUi(false, true, false, false);
    }

    private void showResult(boolean isVerified) {
        updateUi(false, false, true, isVerified);
    }

    private void updateUi(final boolean showProgress, final boolean showMain, final boolean showResult, final boolean isVerified) {
        Log.d("ventsell", TAG + " updateUi = [showProgress: " + showProgress + ", showMain: " + showMain + ", showResult: " + showResult + ", verified: " + isVerified + "]");
        int shortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

        mProgressView.setVisibility(showProgress ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                showProgress ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(showProgress ? View.VISIBLE : View.GONE);
            }
        });
        mMainView.setVisibility(showMain ? View.VISIBLE : View.GONE);
        mMainView.animate().setDuration(shortAnimTime).alpha(
                showMain ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mMainView.setVisibility(showMain ? View.VISIBLE : View.GONE);
            }
        });
        mResultView.setVisibility(showResult ? View.VISIBLE : View.GONE);
        mResultView.animate().setDuration(shortAnimTime).alpha(
                showResult ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mResultView.setVisibility(showResult ? View.VISIBLE : View.GONE);
            }
        });

        if (showResult) {
            mVerifyImageView.setImageResource(isVerified ? R.drawable.img_valid_ticket : R.drawable.img_invalid_ticket);
            mVerifyTextView.setText(context.getResources().getText(isVerified ? R.string.checked_out_text : R.string.invalid_ticket_text));
            mVerifyTextView.setBackgroundColor(ContextCompat.getColor(context, isVerified ? R.color.colorPrimary : R.color.invalid_ticket));
        }
    }

    public boolean resetViews() {
        if (mResultView.getVisibility() == View.VISIBLE) {
            showMain();
            return true;
        }

        return false;
    }
}
