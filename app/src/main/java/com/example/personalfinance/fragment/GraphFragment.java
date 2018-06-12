package com.example.personalfinance.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.personalfinance.R;
import com.example.personalfinance.db.FinanceDb;
import com.example.personalfinance.etc.Const;
import com.example.personalfinance.etc.Utils;
import com.example.personalfinance.model.AccountItem;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GraphFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GraphFragment extends Fragment {

    private static final String ARG_SCOPE = "scope";

    private int mScope;
    private int[] mGraphData;

    private PieChart mGraphView;
    private TextView mEmptyView;
    private TextView mIncomeLegendTextView, mExpenseLegendTextView, mInvestmentLegendTextView;

    public GraphFragment() {
        // Required empty public constructor
    }

    public static GraphFragment newInstance(int scope) {
        GraphFragment fragment = new GraphFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SCOPE, scope);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mScope = getArguments().getInt(ARG_SCOPE);
        }

        mGraphData = new int[]{0, 0, 0};
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_graph, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGraphView = view.findViewById(R.id.graph_view);
        mEmptyView = view.findViewById(R.id.empty_view);

        mIncomeLegendTextView = view.findViewById(R.id.income_legend_text_view);
        mExpenseLegendTextView = view.findViewById(R.id.expense_legend_text_view);
        mInvestmentLegendTextView = view.findViewById(R.id.investment_legend_text_view);

        // configure pie chart
        mGraphView.setUsePercentValues(true);
        Description description = new Description();
        description.setText("");
        mGraphView.setDescription(description);
        mGraphView.getLegend().setEnabled(false);

        // enable hole in pie chart
        mGraphView.setDrawHoleEnabled(true);
        mGraphView.setHoleColor(Color.TRANSPARENT);
        mGraphView.setHoleRadius(7);
        mGraphView.setTransparentCircleRadius(10);

        // enable rotation of the chart by touch
        mGraphView.setRotationAngle(0);
        mGraphView.setRotationEnabled(true);

        // set a chart value selected listener
        mGraphView.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e == null) {
                    return;
                }
            }

            @Override
            public void onNothingSelected() {
            }
        });

/*
        // customize legends
        Legend legend = mGraphView.getLegend();
        //legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setYEntrySpace(2);
        legend.setTextSize(10);
        legend.setTextColor(Color.GRAY);
*/
    }

    @Override
    public void onResume() {
        super.onResume();

        mGraphView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);

        if (loadDataFromDb()) {
            mGraphView.setVisibility(View.VISIBLE);
            drawGraph();
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
        updateLegend();
    }

    private void drawGraph() {
        List<PieEntry> valueList = new ArrayList<>();
        for (int i = 0; i < mGraphData.length; i++) {
            valueList.add(new PieEntry(mGraphData[i], i));
        }

        PieDataSet dataSet = new PieDataSet(valueList, null);
        dataSet.setSliceSpace(3);
        dataSet.setSelectionShift(5);

        List<Integer> colorList = new ArrayList<>();
        colorList.add(getActivity().getResources().getColor(R.color.income));
        colorList.add(getActivity().getResources().getColor(R.color.expense));
        colorList.add(getActivity().getResources().getColor(R.color.investment));
        dataSet.setColors(colorList);

/*
            List<LegendEntry> legendEntryList = new ArrayList<>();
            for (int i = 0; i < mGraphLabels.length; i++) {
                LegendEntry entry = new LegendEntry();
                entry.formColor = colorList.get(i);
                entry.label = mGraphLabels[i];
                legendEntryList.add(entry);
            }
            Legend legend = mGraphView.getLegend();
            legend.setCustom(legendEntryList);
*/

        // instantiate pie data object
        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueTextSize(14);
        pieData.setValueTextColor(Color.WHITE);

        mGraphView.setData(pieData);
        mGraphView.highlightValue(null);
        mGraphView.invalidate();
    }

    private void updateLegend() {
        mIncomeLegendTextView.setText(Utils.formatCurrency(mGraphData[0]));
        mExpenseLegendTextView.setText(Utils.formatCurrency(mGraphData[1]));
        mInvestmentLegendTextView.setText(Utils.formatCurrency(mGraphData[2]));
    }

    private boolean loadDataFromDb() {
        FinanceDb db = new FinanceDb(getContext());
        Calendar calendar = Calendar.getInstance();
        List<AccountItem> accountItemList = new ArrayList<>();

        switch (mScope) {
            case Const.SCOPE_TODAY:
                accountItemList = db.getAccountItemListByDate(calendar.getTime());
                break;
            case Const.SCOPE_YESTERDAY:
                calendar.add(Calendar.DATE, -1);
                accountItemList = db.getAccountItemListByDate(calendar.getTime());
                break;
            case Const.SCOPE_THIS_MONTH:
                accountItemList = db.getAccountItemListByMonth(calendar.getTime());
                break;
        }

        mGraphData[0] = 0; // income
        mGraphData[1] = 0; // expense
        mGraphData[2] = 0; // investment

        if (!accountItemList.isEmpty()) {
            for (AccountItem accountItem : accountItemList) {
                switch (accountItem.type) {
                    case AccountItem.TYPE_INCOME:
                        mGraphData[0] += accountItem.amount;
                        break;
                    case AccountItem.TYPE_EXPENSE:
                        mGraphData[1] += accountItem.amount;
                        break;
                    case AccountItem.TYPE_INVESTMENT:
                        mGraphData[2] += accountItem.amount;
                        break;
                }
            }
            return true;
        } else {
            return false; // no data
        }
    }
}
