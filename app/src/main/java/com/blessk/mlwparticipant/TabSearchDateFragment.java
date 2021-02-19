package com.blessk.mlwparticipant;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import java.util.ArrayList;
import java.util.List;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;

import java.util.Calendar;

import static android.content.Context.CLIPBOARD_SERVICE;

public class TabSearchDateFragment extends Fragment implements OnDateSetListener{

    String selectedLocation;
    String searchTerm;
    TextView searchTextview;
    TextView sortLabel;
    RadioGroup radioGroup;

    private final String search_column="date";
    public static final String DATEPICKER_TAG = "datepicker";

    final int[] fixedColumnWidths = new int[]{35, 31, 13, 13};//new int[]{30, 30, 15, 15};
    final int[] scrollableColumnWidths = new int[]{35, 34, 14, 14};
    final int fixedRowHeight = 70;
    private int fixedHeaderHeight = 65;
    private ProgressDialog mProgressDialog;
    private TableLayout scrollablePart;
    private TableRow.LayoutParams wrapWrapTableRowParams;
    private DatabaseHelper dbHelper;
    private TableLayout header;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view= inflater.inflate(R.layout.tab_search_fragment, container, false);
        return view;
    }
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        context = getActivity();
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        View view=getView();
        TextInputLayout searchViewLayout = (TextInputLayout)view.findViewById(R.id.search_view_layout);
        searchViewLayout.setHint(getResources().getString(R.string.selected_date));

        searchTextview=(TextView)view.findViewById(R.id.search_textview);
        //date
        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH-1), true);
        searchTextview.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                datePickerDialog.setVibrate(true);
                datePickerDialog.setYearRange(2015, 2030);
                datePickerDialog.setCloseOnSingleTapDay(true);
                datePickerDialog.show(getActivity().getSupportFragmentManager(), DATEPICKER_TAG);
            }
        });
        if (savedInstanceState != null) {
            DatePickerDialog dpd = (DatePickerDialog) getActivity().getSupportFragmentManager().findFragmentByTag(DATEPICKER_TAG);
            if (dpd != null) {
                dpd.setOnDateSetListener(this);
            }
        }

        sortLabel=(TextView)view.findViewById(R.id.sort_label);

        //table initialise
        header = (TableLayout) getView().findViewById(R.id.table_header);
        scrollablePart = (TableLayout)getView().findViewById(R.id.scrollable_part);
        wrapWrapTableRowParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        //initialise database helper
        dbHelper=new DatabaseHelper(getContext());

        // setup the sort buttons
        radioGroup = getView().findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                searchTerm=searchTextview.getText().toString();
                performSearch();
            }
        });
        // setup the search button
        Button search_btn=(Button) view.findViewById(R.id.search_btn);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                searchTerm=searchTextview.getText().toString();
                performSearch();
            }
        });
    }
    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        String monthString = String.valueOf(month+1);//months indexed from 0
        String dayString = String.valueOf(day);
        if (monthString.length() == 1) {
            monthString = "0" + monthString;
        }
        if (dayString.length() == 1) {
            dayString = "0" + dayString;
        }
        searchTextview.setText(year + "-" + monthString + "-" + dayString);
    }

    public void setSelectedLocation(String selectedLocation) {
        this.selectedLocation = selectedLocation;
    }
    private void performSearch()
    {
        Context context=getActivity();
        String location=this.selectedLocation;
        String pid=this.searchTerm;
        if(pid.trim().length()==0)
        {
            Toast.makeText(context, "Enter search value ", Toast.LENGTH_LONG).show();
            return;
        }
        int radioButtonID = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = radioGroup.findViewById(radioButtonID);
        final String selectedText =radioButton.getText().toString();
        loadData(location,pid,selectedText);
    }

    private void loadData(String location, String search_text,String sort)
    {
        final Context context=getActivity();

        //destroy table body
        if(scrollablePart.getChildCount() > 0)
        {
            scrollablePart.removeAllViews();
        }
        //remove table header
        if(header.getChildCount() > 0)
        {
            header.removeAllViews();
        }
        final Cursor cursor=dbHelper.getParticipantVisit(location,search_text,search_column,sort);
        TextView searchResultView=getView().findViewById(R.id.searchTextResultView);
        TextView sortView=getView().findViewById(R.id.sort_label);
        if(cursor.getCount()==0)
        {
            String msg="Search \""+search_text+"\" not found.";
            searchResultView.setText(msg);
            sortView.setVisibility(View.GONE);
            radioGroup.setVisibility(View.GONE);
            return;
        }
        else
        {
            String msg=cursor.getCount()+" record(s) found.";
            searchResultView.setText(msg);
            sortView.setVisibility(View.VISIBLE);
            radioGroup.setVisibility(View.VISIBLE);
        }
        if (mProgressDialog == null)
        {
            mProgressDialog=new ProgressDialog(context);
            mProgressDialog.setTitle("Processing...");
            mProgressDialog.setMessage("Searching database, please wait.");
            mProgressDialog.show();
        }
        else
        {
            mProgressDialog.show();
        }
        new Thread(new Runnable()
        {
            public void run()
            {
                final List<ParticipantVisit> visits =processDatabaseRows(cursor);
                getActivity().runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run()
                            {
                                createColumns();
                                fillData(visits);
                                mProgressDialog.dismiss();
                            }
                        });
            }
        }).start();

    }
    private List<ParticipantVisit> processDatabaseRows(Cursor cursor)
    {
        List<ParticipantVisit> visits = new ArrayList<>();
        if (cursor.moveToFirst()){
            do{
                ParticipantVisit visit=new ParticipantVisit();
                visit.setId(cursor.getString(cursor.getColumnIndex("_id")));
                visit.setPid(cursor.getString(cursor.getColumnIndex("pid")));
                visit.setDate(cursor.getString(cursor.getColumnIndex("date")));
                visit.setVisit(cursor.getString(cursor.getColumnIndex("visit")));
                visit.setStatus(cursor.getString(cursor.getColumnIndex("status")));
                visit.setLocation(cursor.getString(cursor.getColumnIndex("location")));
                visits.add(visit);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return visits;
    }
    private void createColumns() {
        Context context=getActivity();

        TableRow rowHeader = new TableRow(context);
        //header (fixed vertically)
        rowHeader.setBackgroundColor(Color.parseColor("#c0c0c0"));
        rowHeader.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        rowHeader.setGravity(Gravity.CENTER);

        String[] headerText={"Date","PID","Visit","Done"};
        int i=0;
        for(String c:headerText)
        {
            rowHeader.addView(makeTableRowWithText(c, fixedColumnWidths[i], fixedHeaderHeight));
            i++;
        }
        header.addView(rowHeader);
        //header (fixed horizontally)

    }
    private void fillData(List<ParticipantVisit> visits)
    {
        final Context context=getActivity();
        //TableLayout fixedColumn = (TableLayout)getView().findViewById(R.id.fixed_column);
        //rest of the table (within a scroll view)

        for(ParticipantVisit v:visits) {
            // Read columns data
            String date= v.getDate();
            String pid= v.getPid();
            String visit= v.getVisit();
            String status= v.getStatus();

            TableRow row = new TableRow(context);
            row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            row.setGravity(Gravity.CENTER);
            row.setBackgroundColor(Color.WHITE);
            row.addView(makeTableRowWithText(date, scrollableColumnWidths[0], fixedRowHeight));
            row.addView(makeTableRowWithText(pid, scrollableColumnWidths[1], fixedRowHeight));
            row.addView(makeTableRowWithText(visit, scrollableColumnWidths[2], fixedRowHeight));
            row.addView(makeTableRowWithText(status, scrollableColumnWidths[3], fixedRowHeight));

            row.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    TableRow currentRow = (TableRow) view;
                    TextView textViewId = (TextView) currentRow.getChildAt(1);
                    String id = textViewId.getText().toString();

                    //showClickedItem(id);
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", id);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, "PID "+id+" Copied to clipboard", Toast.LENGTH_LONG).show();
                }
            });
            scrollablePart.addView(row, new TableLayout.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
        }
    }

    //util method
    private TextView recyclableTextView;

    public TextView makeTableRowWithText(String text, int widthInPercentOfScreenWidth, int fixedHeightInPixels) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        recyclableTextView = new TextView(getActivity());
        recyclableTextView.setText(text);
        recyclableTextView.setTextColor(Color.BLACK);
        recyclableTextView.setTextSize(18);
        recyclableTextView.setWidth(widthInPercentOfScreenWidth * screenWidth / 100);
        recyclableTextView.setHeight(fixedHeightInPixels);
        return recyclableTextView;
    }
}
