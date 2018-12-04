package com.example.junmung.studyhelper.view;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.junmung.studyhelper.BR;


public abstract class MyBaseAdapter extends RecyclerView.Adapter<MyBaseAdapter.BaseViewHolder> {
    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, viewType, parent, false);
        BaseViewHolder holder = new BaseViewHolder(binding);
        holder.bind(this);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        Object obj = getObjForPosition(position);
        holder.bind(obj);
    }

    @Override
    public int getItemViewType(int position) {
        return getLayoutId();
    }

    protected abstract Object getObjForPosition(int position);

    protected abstract int getLayoutId();


    public static class BaseViewHolder extends RecyclerView.ViewHolder {
        public final ViewDataBinding binding;

        public BaseViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Object obj){
            binding.setVariable(BR.obj, obj);
        }

        public void bind(MyBaseAdapter adapter) {
            binding.setVariable(BR.adapter, adapter);
//            binding.executePendingBindings();
        }
    }
}
