package com.example.clicker.objectbo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clicker.MainActivity;
import com.example.clicker.ObjectBoxApp;
import com.example.clicker.R;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;

public class PointListAdapter extends RecyclerView.Adapter<PointListAdapter.PointListHolder> {
    public static final int MODE_ADD = 0;
    public static final int MODE_EDIT = 1;
    public static final int MODE_DELETE = 2;

    private Context context;
    private List<Point> pointList;

    private BoxStore boxStore;
    private Box<Point> pointBox;

    public PointListAdapter(Context context, List<Point> pointList) {
        this.context = context;
        this.pointList = pointList;
        boxStore = ((ObjectBoxApp) context).getBoxStore();
        pointBox = boxStore.boxFor(Point.class);

    }

    public PointListAdapter(Context context) {
        this.context = context;
        boxStore = ((ObjectBoxApp) context).getBoxStore();
        pointBox = boxStore.boxFor(Point.class);
    }

    @Override
    public PointListAdapter.PointListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).
                inflate(R.layout.row_point_list, parent, false);
        return new PointListHolder(itemView);

    }

    @Override
    public void onBindViewHolder(PointListAdapter.PointListHolder holder, int position) {
        final Point point = pointList.get(position);
        holder.tvPointWithJersey.setText(point.getName() + " " + point.getContactType() + " (" + point.getLat() + ":" + point.getLon() + ")");
    }

    @Override
    public int getItemCount() {
        int i = pointList.size();
        return pointList.size();
    }

    public String getDailyCatch() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        return Long.toString(pointBox.query().equal(Point_.contactType, "CATCH").greater(Point_.timeStamp, today.getTime()).build().count());
    }

    public String getDailyContact() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        return Long.toString(pointBox.query().equal(Point_.contactType, "CONTACT").greater(Point_.timeStamp, today.getTime()).build().count());
    }

    public String getDailyFollow() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        return Long.toString(pointBox.query().equal(Point_.contactType, "FOLLOW").greater(Point_.timeStamp, today.getTime()).build().count());
    }

    public String getTripCatch() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int tripLength = Integer.parseInt(prefs.getString("TripLength", "0"));
        Calendar today = GregorianCalendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.add(Calendar.DATE, 0 - tripLength);
        return Long.toString(pointBox.query().equal(Point_.contactType, "CATCH").greater(Point_.timeStamp, today.getTime()).build().count());
    }

    public String getTripContact() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int tripLength = Integer.parseInt(prefs.getString("TripLength", "0"));
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.add(Calendar.DATE, 0 - tripLength);
        return Long.toString(pointBox.query().equal(Point_.contactType, "CONTACT").greater(Point_.timeStamp, today.getTime()).build().count());
    }

    public String getTripFollow() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int tripLength = Integer.parseInt(prefs.getString("TripLength", "0"));
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.add(Calendar.DATE, 0 - tripLength);
        return Long.toString(pointBox.query().equal(Point_.contactType, "FOLLOW").greater(Point_.timeStamp, today.getTime()).build().count());
    }

    public String getTotalCatch() {
        return Long.toString(pointBox.query().equal(Point_.contactType, "CATCH").build().count());
    }

    public String getTotalContact() {
        return Long.toString(pointBox.query().equal(Point_.contactType, "CONTACT").build().count());
    }

    public String getTotalFollow() {
        return Long.toString(pointBox.query().equal(Point_.contactType, "FOLLOW").build().count());
    }

    public class PointListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView tvPointWithJersey;
        protected ImageButton btnEdit, btnDelete;

        public PointListHolder(View itemView) {
            super(itemView);
            tvPointWithJersey = (TextView) itemView.findViewById(R.id.tvPlayerWithJersey);
            btnEdit = (ImageButton) itemView.findViewById(R.id.btnEdit);
            btnDelete = (ImageButton) itemView.findViewById(R.id.btnDelete);
            btnEdit.setOnClickListener(this);
            btnDelete.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnEdit:
                    showAddEditDialog(MODE_EDIT, pointList.get(getAdapterPosition()).getId());
                    break;
                case R.id.btnDelete:
                    showAddEditDialog(MODE_DELETE, pointList.get(getAdapterPosition()).getId());
                    break;
            }

        }
    }

    /**
     * Shows a simple dialog to add/edit/delete
     *
     * @param mode mode of operation i.e, add/edit/delete
     * @param id   id of the player, 0 for new player
     */
    public void showAddEditDialog(final int mode, final long id) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = ((MainActivity) context).getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_point_add, null);
        dialogBuilder.setView(dialogView);

        final EditText etPointName = (EditText) dialogView.findViewById(R.id.etPlayerName);
        final EditText etJerseyNumber = (EditText) dialogView.findViewById(R.id.etJerseyNumber);

        String title = "";
        switch (mode) {
            case MODE_ADD:
                title = "Add Point";
                break;
            case MODE_EDIT:
                title = "Edit Point";
                Point pointToEdit = getPointById(id);
                etPointName.setText(pointToEdit.getName());
                etJerseyNumber.setText(String.valueOf(pointToEdit.getName()));
                break;
            case MODE_DELETE:
                title = "Delete Point";
                Point pointToDelete = getPointById(id);
                etPointName.setText(pointToDelete.getName());
                etJerseyNumber.setText(String.valueOf(pointToDelete.getName()));
                etPointName.setKeyListener(null);
                etJerseyNumber.setKeyListener(null);
                break;
            default:
                break;
        }
        dialogBuilder.setTitle(title);

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                switch (mode) {
                    case MODE_ADD:
                        if (etPointName.getText().toString().trim().isEmpty() || etJerseyNumber.getText().toString().trim().isEmpty()) {
                            Toast.makeText(context, "Name and Jersey number can not be empty.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        addOrUpdatePoint(new Point(0, etPointName.getText().toString().trim(), "", 0.0, 0.0));
                        updatePoints();
                        break;
                    case MODE_EDIT:
                        addOrUpdatePoint(new Point(id, etPointName.getText().toString().trim(), "", 0.0, 0.0));
                        updatePoints();
                        break;
                    case MODE_DELETE:
                        deletePoint(id);
                        updatePoints();
                        break;
                    default:
                        break;
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do nothing
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void setPoints(List<Point> pointList) {
        this.pointList = pointList;
        notifyDataSetChanged();
    }
    //Database related operation here

    private Point getPointById(long id) {
        return pointBox.query().equal(Point_.id, id).build().findUnique();
    }

    public void addOrUpdatePoint(Point point) {
        pointBox.put(point);
    }

    public void clearPoints() {
        pointBox.removeAll();
    }

    private void deletePoint(long id) {
        Point player = getPointById(id);
        if (player != null) {
            pointBox.remove(id);
        }
    }

    public void updatePoints() {
        List<Point> points = pointBox.query().build().find();
        this.setPoints(points);
    }
}
