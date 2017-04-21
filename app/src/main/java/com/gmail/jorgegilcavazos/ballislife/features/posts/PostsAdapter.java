package com.gmail.jorgegilcavazos.ballislife.features.posts;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.features.model.SubscriberCount;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;
import com.gmail.jorgegilcavazos.ballislife.features.shared.FullCardViewHolder;
import com.gmail.jorgegilcavazos.ballislife.features.shared.OnSubmissionClickListener;
import com.gmail.jorgegilcavazos.ballislife.network.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;
import com.squareup.picasso.Picasso;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTENT = 1;
    private static final int TYPE_LOADING = 2;

    private Context context;
    private List<CustomSubmission> postsList;
    private PostsFragment.ViewType contentViewType;
    private OnSubmissionClickListener submissionClickListener;
    private SubscriberCount subscriberCount;
    private OnLoadMoreListener loadMoreListener;
    private boolean isLoading = false;
    private boolean loadingFailed = false;

    public PostsAdapter(Context context,
                        List<CustomSubmission> postsList,
                        PostsFragment.ViewType contentViewType,
                        OnSubmissionClickListener submissionClickListener) {
        this.context = context;
        this.postsList = postsList;
        this.contentViewType = contentViewType;
        this.submissionClickListener = submissionClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else if (position == getItemCount() - 1) {
            return TYPE_LOADING;
        }

        return TYPE_CONTENT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view;

        if (viewType == TYPE_HEADER) {
            view = inflater.inflate(R.layout.rnba_header_layout, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == TYPE_LOADING) {
            view = inflater.inflate(R.layout.row_load_more, parent, false);
            return new LoadHolder(view);
        }

        switch (contentViewType) {
            case FULL_CARD:
                view = inflater.inflate(R.layout.post_layout_large, parent, false);
                return new FullCardViewHolder(view);
            default:
                view = inflater.inflate(R.layout.post_layout_large, parent, false);
                return new FullCardViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bindData(context, subscriberCount);
        } else if (holder instanceof LoadHolder) {
            LoadHolder loadHolder = (LoadHolder) holder;
            // Load more items if scroll position is last and is not already loading.
            if (position >= getItemCount() - 1 && !isLoading && !loadingFailed && loadMoreListener != null
                    && postsList != null && !postsList.isEmpty()) {
                isLoading = true;
                loadMoreListener.onLoadMore();
            }

            if (isLoading) {
                loadHolder.progressBar.setVisibility(View.VISIBLE);
            } else {
                loadHolder.progressBar.setVisibility(View.GONE);
            }
        } else {
            CustomSubmission customSubmission = postsList.get(position - 1);
            switch (contentViewType) {
                case FULL_CARD:
                    ((FullCardViewHolder) holder).bindData(context, customSubmission, true,
                            submissionClickListener);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return null != postsList ? postsList.size() + 2 : 2;
    }

    public void notifyDataChanged() {
        isLoading = false;
        notifyDataSetChanged();
    }

    public void setData(List<CustomSubmission> submissions) {
        loadingFailed = false;
        postsList = submissions;
        notifyDataChanged();
    }

    public void addData(List<CustomSubmission> submissions) {
        if (postsList != null) {
            loadingFailed = false;
            postsList.addAll(submissions);
            notifyDataChanged();
        }
    }

    public void setSubscriberCount(SubscriberCount subscriberCount) {
        this.subscriberCount = subscriberCount;
        notifyDataChanged();
    }

    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    public void setLoadingFailed(boolean failed) {
        loadingFailed = failed;
        notifyDataChanged();
    }

    interface OnLoadMoreListener {
        void onLoadMore();
    }

    /* View Holders **/

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_subscribers) TextView tvSubscribers;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(Context context, SubscriberCount subscriberCount) {
            if (subscriberCount != null) {
                String subscribers = String.valueOf(subscriberCount.getSubscribers());
                String activeUsers = String.valueOf(subscriberCount.getActiveUsers());

                tvSubscribers.setText(context.getString(R.string.subscriber_count,
                        subscribers, activeUsers));
            } else {
                tvSubscribers.setText(context.getString(R.string.subscriber_count,
                        String.valueOf(554843), String.valueOf(8133)));
            }
        }
    }

    static class LoadHolder extends  RecyclerView.ViewHolder {

        @BindView(R.id.progressBar) ProgressBar progressBar;

        public LoadHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private void setFullCardViews(final FullCardViewHolder holder,
                                  final CustomSubmission customSubmission) {
        final Submission submission = customSubmission.getSubmission();

        holder.tvBody.setVisibility(View.GONE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            holder.tvTitle.setText(Html.fromHtml(submission.getTitle(), Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.tvTitle.setText(Html.fromHtml(submission.getTitle()));
        }

        if (submission.isStickied()) {
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.stickiedColor));
            holder.tvTitle.setTypeface(null, Typeface.BOLD);
        } else {
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.primaryText));
            holder.tvTitle.setTypeface(null, Typeface.NORMAL);
        }

        if (submission.getVote() == VoteDirection.UPVOTE) {
            RedditUtils.setUpvotedColors(context, holder);
        } else if (submission.getVote() == VoteDirection.DOWNVOTE) {
            RedditUtils.setDownvotedColors(context, holder);
        } else {
            RedditUtils.setNoVoteColors(context, holder);
        }

        if (submission.isSaved()) {
            RedditUtils.setSavedColors(context, holder);
        } else {
            RedditUtils.setUnsavedColors(context, holder);
        }

        holder.tvPoints.setText(String.valueOf(submission.getScore()));
        holder.tvAuthor.setText(submission.getAuthor());
        holder.tvTimestamp.setText(DateFormatUtil.formatRedditDate(submission.getCreated()));
        holder.tvComments.setText(String.valueOf(submission.getCommentCount()));

        String highResThumbnailUrl;
        try {
            highResThumbnailUrl = submission.getOEmbedMedia().getThumbnail().getUrl().toString();
        } catch (NullPointerException e) {
            highResThumbnailUrl = submission.getThumbnail();
        }

        if (submission.isSelfPost()) {
            holder.tvDomain.setText("self");
            holder.ivThumbnail.setVisibility(View.GONE);
            holder.containerLink.setVisibility(View.GONE);
        } else {
            String domain = submission.getDomain();
            holder.tvDomain.setText(domain);
            if (highResThumbnailUrl != null) {
                holder.ivThumbnail.setVisibility(View.VISIBLE);
                holder.containerLink.setVisibility(View.GONE);
                Picasso.with(context)
                        .load(highResThumbnailUrl)
                        .into(holder.ivThumbnail);

                // TODO: add view to play content

                if (domain.equals(Constants.YOUTUBE_DOMAIN)
                        || domain.equals(Constants.INSTAGRAM_DOMAIN)
                        || domain.equals(Constants.STREAMABLE_DOMAIN)) {
                    //thumbnailType.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                } else if (domain.equals(Constants.IMGUR_DOMAIN )
                        || domain.equals(Constants.GIPHY_DOMAIN)) {
                    //thumbnailType.setImageResource(R.drawable.ic_gif_black_24dp);
                } else {
                    //thumbnailType.setVisibility(View.GONE);
                }
            } else {
                holder.ivThumbnail.setVisibility(View.GONE);
                holder.containerLink.setVisibility(View.VISIBLE);
                holder.tvDomainLink.setText(submission.getDomain());
                holder.tvLink.setText(submission.getUrl());
            }
        }

        holder.btnUpvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customSubmission.getVoteDirection() == VoteDirection.UPVOTE) {
                    submissionClickListener.onVoteSubmission(submission, VoteDirection.NO_VOTE);
                    if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.NO_VOTE);
                        RedditUtils.setNoVoteColors(context, holder);
                        holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                                holder.tvPoints.getText().toString()) - 1));
                    }
                } else if (customSubmission.getVoteDirection() == VoteDirection.DOWNVOTE) {
                    submissionClickListener.onVoteSubmission(submission, VoteDirection.UPVOTE);
                    if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.UPVOTE);
                        RedditUtils.setUpvotedColors(context, holder);
                        holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                                holder.tvPoints.getText().toString()) + 2));
                    }
                } else {
                    submissionClickListener.onVoteSubmission(submission, VoteDirection.UPVOTE);
                    if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.UPVOTE);
                        RedditUtils.setUpvotedColors(context, holder);
                        holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                                holder.tvPoints.getText().toString()) + 1));
                    }
                }
            }
        });

        holder.btnDownvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customSubmission.getVoteDirection() == VoteDirection.DOWNVOTE) {
                    submissionClickListener.onVoteSubmission(submission, VoteDirection.NO_VOTE);
                    if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.NO_VOTE);
                        RedditUtils.setNoVoteColors(context, holder);
                        holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                                holder.tvPoints.getText().toString()) + 1));
                    }
                } else if (customSubmission.getVoteDirection() == VoteDirection.UPVOTE){
                    submissionClickListener.onVoteSubmission(submission, VoteDirection.DOWNVOTE);
                    if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.DOWNVOTE);
                        RedditUtils.setDownvotedColors(context, holder);
                        holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                                holder.tvPoints.getText().toString()) - 2));
                    }
                } else {
                    submissionClickListener.onVoteSubmission(submission, VoteDirection.DOWNVOTE);
                    if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.DOWNVOTE);
                        RedditUtils.setDownvotedColors(context, holder);
                        holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                                holder.tvPoints.getText().toString()) - 1));
                    }
                }
            }
        });

        holder.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customSubmission.isSaved()) {
                    submissionClickListener.onSaveSubmission(submission, false);
                    if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                        RedditUtils.setUnsavedColors(context, holder);
                        customSubmission.setSaved(false);
                    }
                } else {
                    submissionClickListener.onSaveSubmission(submission, true);
                    if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                        RedditUtils.setSavedColors(context, holder);
                        customSubmission.setSaved(true);
                    }
                }
            }
        });

        holder.btnComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submissionClickListener.onSubmissionClick(submission);
            }
        });

        holder.tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submissionClickListener.onSubmissionClick(submission);
            }
        });

        holder.ivThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submissionClickListener.onContentClick(submission.getUrl());
            }
        });

        holder.containerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submissionClickListener.onContentClick(submission.getUrl());
            }
        });
    }

}
