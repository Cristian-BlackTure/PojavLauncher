package net.kdt.pojavlaunch.multirt;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import java.io.IOException;
import java.util.List;

public class RTRecyclerViewAdapter extends RecyclerView.Adapter {
    MultiRTConfigDialog dialog;
    public RTRecyclerViewAdapter(MultiRTConfigDialog dialog) {
        this.dialog = dialog;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View recyclableView = LayoutInflater.from(parent.getContext()).inflate(R.layout.multirt_recyclable_view,parent,false);
        return new RTViewHolder(recyclableView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final List<MultiRTUtils.Runtime> runtimes = MultiRTUtils.getRuntimes();
        ((RTViewHolder)holder).bindRuntime(runtimes.get(position),position);
    }
    public boolean isDefaultRuntime(MultiRTUtils.Runtime rt) {
        return LauncherPreferences.PREF_DEFAULT_RUNTIME.equals(rt.name);
    }
    @Override
    public int getItemCount() {
        return MultiRTUtils.getRuntimes().size();
    }
    public class RTViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final TextView javaVersionView;
        final TextView fullJavaVersionView;
        final ColorStateList defaultColors;
        final Button setDefaultButton;
        final Context ctx;
        MultiRTUtils.Runtime currentRuntime;
        int currentPosition;
        public RTViewHolder(View itemView) {
            super(itemView);
            javaVersionView = itemView.findViewById(R.id.multirt_view_java_version);
            fullJavaVersionView = itemView.findViewById(R.id.multirt_view_java_version_full);
            itemView.findViewById(R.id.multirt_view_removebtn).setOnClickListener(this);
            setDefaultButton = itemView.findViewById(R.id.multirt_view_setdefaultbtn);
            setDefaultButton.setOnClickListener(this);
            defaultColors =  fullJavaVersionView.getTextColors();
            ctx = itemView.getContext();
        }
        public void bindRuntime(MultiRTUtils.Runtime rt, int pos) {
            currentRuntime = rt;
            currentPosition = pos;
            if(rt.versionString != null) {
                javaVersionView.setText(ctx.getString(R.string.multirt_java_ver, rt.name, rt.javaVersion));
                fullJavaVersionView.setText(rt.versionString);
                fullJavaVersionView.setTextColor(defaultColors);
                setDefaultButton.setVisibility(View.VISIBLE);
                boolean default_ = isDefaultRuntime(rt);
                setDefaultButton.setEnabled(!default_);
                setDefaultButton.setText(default_?R.string.multirt_config_setdefault_already:R.string.multirt_config_setdefault);
            }else{
                javaVersionView.setText(rt.name);
                fullJavaVersionView.setText(R.string.multirt_runtime_corrupt);
                fullJavaVersionView.setTextColor(Color.RED);
                setDefaultButton.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.multirt_view_removebtn) {
                if (currentRuntime != null) {
                    final ProgressDialog barrier = new ProgressDialog(ctx);
                    barrier.setMessage(ctx.getString(R.string.global_waiting));
                    barrier.setProgressStyle(barrier.STYLE_SPINNER);
                    barrier.setCancelable(false);
                    barrier.show();
                    Thread t = new Thread(() -> {
                        try {
                            MultiRTUtils.removeRuntimeNamed(currentRuntime.name);
                        } catch (IOException e) {
                            Tools.showError(itemView.getContext(), e);
                        }
                        v.post(() -> {
                            barrier.dismiss();
                            RTRecyclerViewAdapter.this.notifyItemRemoved(currentPosition);
                            dialog.dialog.show();
                        });
                    });
                    t.start();
                }
            }else if(v.getId() == R.id.multirt_view_setdefaultbtn) {
                if(currentRuntime != null) {
                    LauncherPreferences.PREF_DEFAULT_RUNTIME = currentRuntime.name;
                    LauncherPreferences.DEFAULT_PREF.edit().putString("defaultRuntime",LauncherPreferences.PREF_DEFAULT_RUNTIME).apply();
                    RTRecyclerViewAdapter.this.notifyDataSetChanged();
                }
            }
        }
    }
}
