package com.example.junmung.studyhelper.memo;

import com.example.junmung.studyhelper.data.Memo;

public interface MemoClickListener {
    void onClick(Memo memo);
    boolean onLongClick(Memo memo);
}
