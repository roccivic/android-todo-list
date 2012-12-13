package com.placella.todo;

import java.util.List;

import org.json.*;

import android.os.*;
import android.app.*;
import android.content.Intent;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.view.View.*;
import android.widget.*;

public class Activity_Main extends Activity {
	private final ToDoList todo = new ToDoList(this);
	private final Activity_Main self = this;
	private final int dataListId = 0x00ffff00;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout l = new LinearLayout(self);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setLayoutParams(
        	new LayoutParams(
        		LayoutParams.MATCH_PARENT,
        		LayoutParams.MATCH_PARENT
        	)
        );
        l.setPadding(10, 10, 10, 10);
        
        TextView t = new TextView(self);
        t.setText(R.string.app_name);
        t.setTextSize(20);
        t.setLayoutParams(
        	new LayoutParams(
        		LayoutParams.MATCH_PARENT,
        		LayoutParams.WRAP_CONTENT
        	)
        );
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 0, 0, 10);
        l.addView(t);
        
        ScrollView s = new ScrollView(self);
        s.setLayoutParams(
        	new LayoutParams(
        		LayoutParams.MATCH_PARENT,
        		LayoutParams.MATCH_PARENT
        	)
        );
        s.addView(getList());
    	l.addView(s);
        
		setContentView(l);
	}
	
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	if (requestCode == REQUEST.EDIT && resultCode == RESPONSE.MODIFIED) {
	        Item item = (Item) intent.getSerializableExtra("item");
	    	if (todo.replace(item)) {
	    		refresh();
	    	} else {
	    		new Dialog_Message(self, R.string.dberror).show();
	    	}
    	} else if (requestCode == REQUEST.EDIT && resultCode == RESPONSE.DELETED) {
	        Item item = (Item) intent.getSerializableExtra("item");
	        if (todo.delete(item)) {
	    		refresh();
	    	} else {
	    		new Dialog_Message(self, R.string.dberror).show();
	    	}
    	} else if (requestCode == REQUEST.ADD && resultCode == RESPONSE.ADDED) {
	        Item item = (Item) intent.getSerializableExtra("item");
	        if (todo.add(item)) {
	    		refresh();
	    	} else {
	    		new Dialog_Message(self, R.string.dberror).show();
	    	}
    	}
    }
	
	public LinearLayout getList() {
		TextView t;
		List<Item> mainList = todo.getList();
		LinearLayout l = new LinearLayout(self);
		l.setId(dataListId);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setLayoutParams(
        	new LayoutParams(
        		LayoutParams.MATCH_PARENT,
        		LayoutParams.MATCH_PARENT
        	)
        );
		if (mainList.size() > 0) {
			Util.hr(l, this);
			for (Item i : mainList) {
				LinearLayout innerLayout = new LinearLayout(self);
				innerLayout.setOrientation(LinearLayout.HORIZONTAL);
				innerLayout.setTag(i.getId());

				ImageView img = new ImageView(self);
				if (i.getType() == Item.NOTE) {
					img.setImageDrawable(getResources().getDrawable(R.drawable.ic_note));
				} else {
					img.setImageDrawable(getResources().getDrawable(R.drawable.ic_list));
				}
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(52, 52);
				img.setLayoutParams(layoutParams);
				img.setPadding(0, 10, 0, 10);
				innerLayout.addView(img);
				
				TextView b = new TextView(this);
				String name = i.getName();
				if (name.length() == 0) {
					name = getResources().getString(R.string.noname);
				}
				b.setText(name);
	            b.setTextSize(16);
	            b.setPadding(0, 10, 0, 10);

	            innerLayout.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Item i = todo.find(arg0.getTag());
						Class<?> target;
						if (i.getType() == Item.NOTE) {
							target = Activity_ViewNote.class;
						} else {
							target = Activity_ViewList.class;
						}
				        Bundle b = new Bundle();
				        b.putInt("action", REQUEST.EDIT);
				        b.putSerializable("item", i);
				        Intent intent = new Intent(self, target);
				        intent.putExtras(b);
				        startActivityForResult(intent, REQUEST.EDIT);
					}
				});
	            innerLayout.addView(b);
	            l.addView(innerLayout);
				Util.hr(l, this);
			}
		} else {
			t = new TextView(self);
			t.setText(getResources().getString(R.string.no_items));
			l.addView(t);
			t = new TextView(self);
			t.setText(getResources().getString(R.string.new_hint));
            t.setTextSize(10);
			l.addView(t);
		}
		return l;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_add) {
	        Bundle b = new Bundle();
	        b.putInt("action", REQUEST.ADD);
	        Intent intent = new Intent(self, Activity_Add.class);
	        intent.putExtras(b);
	        startActivityForResult(intent, REQUEST.ADD);
        } else if (item.getItemId() == R.id.menu_sync) {
        	new Dialog_Message(self, createJson()).show();
        }
        return super.onOptionsItemSelected(item);
    }
	
	@Override
    public void onPause() {
        super.onPause();
    	this.todo.close();
    }
	
	public String createJson() {
    	JSONArray root = new JSONArray();
        try {
        	for (Item i : todo.getList()) {
                JSONObject itemObj = new JSONObject();
                itemObj.put("name", i.getName());
                itemObj.put("type", i.getType());
                itemObj.put("note", i.getNotecontent());
            	JSONArray array = new JSONArray();
            	for (Item inner : i.getListcontent()) {
                    JSONObject innerObj = new JSONObject();
            		innerObj.put("txt", inner.getName());
            		innerObj.put("sel", inner.getState());
            		array.put(innerObj);
            	}
            	itemObj.put("list", array);
            	root.put(itemObj);
        	}
		} catch (JSONException e) {
			e.printStackTrace();
		}
        return root.toString();
	}
    
    public void refresh() {
	    LinearLayout l = (LinearLayout) findViewById(dataListId);
	    l.removeAllViews();
	    
        ScrollView s = new ScrollView(self);
        s.setLayoutParams(
        	new LayoutParams(
        		LayoutParams.MATCH_PARENT,
        		LayoutParams.MATCH_PARENT
        	)
        );
        s.addView(getList());
    	l.addView(s);
    }

}
