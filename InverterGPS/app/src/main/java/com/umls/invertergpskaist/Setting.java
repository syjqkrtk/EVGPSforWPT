package com.umls.invertergpskaist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


public class Setting extends AppCompatActivity {

    private ListAdapter adapter;
    private Button buttonAdd;
    private Button buttonRemove;
    private Button buttonSave;
    private Button buttonCancel;
    private ListView listView;
    private String DEFAULT_PHONE = "01012345678";
    public String[] set_PhoneNum;
    private int num;

    public ArrayList<EditText> find = new ArrayList<>();
    public ArrayList<ListItem> listViewItemList = new ArrayList<ListItem>(); //리스트뷰
    private ArrayList<ListItem> filteredItemList = listViewItemList; //리스트뷰 임시저장소
    public ArrayList<String>find2 = new ArrayList<>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.setting_popup);

        Intent intent = getIntent();
        set_PhoneNum = intent.getStringArrayExtra("phoneNum");
        num = intent.getIntExtra("num",1);

        buttonAdd = (Button)findViewById(R.id.buttonAdd);
        buttonRemove = (Button)findViewById(R.id.buttonRemove);
        buttonSave = (Button)findViewById(R.id.buttonSave);
        buttonCancel = (Button)findViewById(R.id.buttonCancel);
        listView=(ListView)findViewById(R.id.listview);

        adapter = new ListAdapter();

        adapter.setItem(set_PhoneNum,num);
        listView.setAdapter(adapter);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Setting.this,(num+1)+ "번이 추가되었습니다.", Toast.LENGTH_SHORT).show();
                adapter.addItem(DEFAULT_PHONE+"",num);
                adapter.notifyDataSetChanged();
                if (num < 10) {
                    num ++;
                }
            }
        });

        buttonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Setting.this, (num)+"번이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                adapter.delItem();
                adapter.notifyDataSetChanged();
                if (num > 1) {
                    num --;
                }
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set_PhoneNum = adapter.getString();
                Intent intent = new Intent();
                intent.putExtra("phoneNum", set_PhoneNum);
                intent.putExtra("num",num);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Setting.this, "취소하여 돌아갑니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    //어뎁터 시작
    public class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return filteredItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return filteredItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Context context = parent.getContext();
            final ViewHolder holder;

            // "listview_item" Layout을 inflate하여 convertView 참조 획득.
            if (convertView == null) {
                holder = new ViewHolder();
                LayoutInflater inflater =
                        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.lyt_listview_list, parent, false);
                holder.editText = (EditText)convertView.findViewById(R.id.editText);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }

            holder.ref = position;

            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            final TextView textView = (TextView) convertView.findViewById(R.id.textView1);
            final EditText editText = (EditText)convertView.findViewById(R.id.editText);

            // Data Set(filteredItemList)에서 position에 위치한 데이터 참조 획득
            final ListItem listViewItem = filteredItemList.get(position);

            textView.setText("Car "+String.valueOf(position+1)+" / Phone Number : ");
            holder.editText.setText(listViewItem.getPhone());

            holder.editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    filteredItemList.get(holder.ref).setPhone(s.toString());
                }
            });

            return convertView;
        }

        public void addItem(String phone, int num) {
            if (listViewItemList.size() > 9){
            } else {
                ListItem item = new ListItem();
                item.setPhone(phone);
                item.setNum(num);

                listViewItemList.add(item);
            }
        }

        public void setItem(String[] phone, int num) {
            for (int i = 0;i<num;i++) {
                    ListItem item = new ListItem();
                    item.setPhone(phone[i]);
                    item.setNum(num);

                    listViewItemList.add(item);
            }
        }

        public String[] getString() {
            String[] result = new String[10];
            for (int i = 0;i<listViewItemList.size();i++) {
                result[i] = listViewItemList.get(i).getPhone();
            }
            return result;
        }

        public void delItem() {
            if (listViewItemList.size() < 2) {
            } else {
                listViewItemList.remove(listViewItemList.size() - 1);
            }
        }
    }
}
