package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * RemoteViewsService controlling the data being shown in the scrollable Football Scores detail widget
 * This is derived from the Sunshine app code for widgets originally released as open source software. see below:
 * https://github.com/udacity/Advanced_Android_Development
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();
    private String[] widgetdate = new String[1];
    // these indices must match the projection
    public static final int COL_DATE = 1;
    public static final int COL_MATCHTIME = 2;
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_LEAGUE = 5;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_ID = 8;
    public static final int COL_MATCHDAY = 9;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                //Get current date so that we only get match information for today
                Date wdate = new Date(System.currentTimeMillis());
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                widgetdate[0] = mformat.format(wdate);
                //call Database Provider to get today's match data cursor
                data = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(), null, null, widgetdate, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                views.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(
                        data.getString(COL_HOME)));
                views.setImageViewResource(R.id.away_crest, Utilies.getTeamCrestByTeamName(
                        data.getString(COL_AWAY)));

                String descriptionHome = data.getString(COL_HOME);
                String descriptionAway = data.getString(COL_AWAY);

                views.setTextViewText(R.id.home_name, descriptionHome);
                views.setTextViewText(R.id.away_name, descriptionAway);
                views.setTextViewText(R.id.data_textview, data.getString(COL_MATCHTIME));

                String score = Utilies.getScores(data.getInt(COL_HOME_GOALS), data.getInt(COL_AWAY_GOALS));
                views.setTextViewText(R.id.score_textview, score);

                //Set Content views for Ally support.
                String against = getString(R.string.share_text);
                String directions = getString(R.string.view_directions);
                String describeView = data.getString(COL_HOME)+" " + against +" "+ data.getString(COL_AWAY) + " " + score +" " +  directions;

                //If Android Version is greater than 4.0.3 proceed to set content descriptions for views
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, describeView);

                }

                //fillin intent for Parent Pending Intent so that clicking on any item will launch main activity.
                final Intent fillInIntent = new Intent();
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {

                views.setContentDescription(R.id.widget_list_item, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
//                if (data.moveToPosition(position))
//                    return data.getLong(INDEX_WEATHER_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
