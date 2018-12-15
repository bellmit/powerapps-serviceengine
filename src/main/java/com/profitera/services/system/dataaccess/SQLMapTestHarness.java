package com.profitera.services.system.dataaccess;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.profitera.ibatis.SQLMapResource;
import com.profitera.ibatis.StatementDetail;

public class SQLMapTestHarness
{
    private class RunQuery
        implements Runnable
    {
      boolean isErrorFree = true; 

        public void run()
        {
            for(int i = 0; i < QueryNames.length; i++)
            {
                getCmbName().setSelectedValue(QueryNames[i], true);
                try
                {
                    myRun(QueryNames[i].toString());
                    continue;
                }
                catch(Exception e)
                {
                  isErrorFree = false;
                    printStackTrace(e);
                    break;
                }
            }

        }

        private Object QueryNames[];

        public RunQuery(Object QueryNames[])
        {
            this.QueryNames = QueryNames;
        }
    }

    private class RefreshRun
        implements Runnable
    {

        public void run()
        {
            Object myIndex = getCmbName().getSelectedValue();
            cmbName.setListData(new String[0]);
            butLoop.setEnabled(false);
            reload();
            reloadSqlMapReadOnlyProvider();
            Object tempMap[] = getResultMaps().keySet().toArray();
            Arrays.sort(tempMap);
            cmbName.setListData(tempMap);
            getCmbName().setSelectedValue(myIndex, true);
            butLoop.setEnabled(true);
            getTxtError().setText("");
        }

        RefreshRun()
        {
        }
    }
    private boolean headless;


    public SQLMapTestHarness(File argPath, boolean justVerify)
    {
      this.headless = justVerify;
        argumentsPath = argPath;
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("SQLMapTestHarness");
        frame.setDefaultCloseOperation(3);
        frame.getContentPane().add(getComponent());
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        if (headless){
          Object tempMap[] = getResultMaps().keySet().toArray();
          Arrays.sort(tempMap);
          RunQuery rq = new RunQuery(tempMap);
          rq.run();
          if (rq.isErrorFree){
            System.exit(0);
          } else {
            System.err.println(getTxtError().getText());
            System.exit(1);
          }
        } else {
          frame.setVisible(true);
          frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    public static void main(String args[])
    {
      if (args.length == 0){
        System.out.println("Usage: java " + SQLMapTestHarness.class.getName() + " <data directory> [--verify]");
        return;
      }
        File argPath = new File(args[0]);
        if(!argPath.isDirectory())
        {
          System.out.println("Usage: java " + SQLMapTestHarness.class.getName() + " <data directory>");
          return;
        } else {
          boolean v = args.length > 1 && args[1].equals("--verify");
          new SQLMapTestHarness(argPath, v);
          return;
        }
    }

    private SQLMapResource getSQLMapResource()
    {
        if(r == null)
            r = new SQLMapResource("Test", new Properties(), "server.xml");
        return r;
    }

    private Map getResultMaps()
    {
        if(resultMaps == null){
          Map m = getSQLMapResource().getStatementDetails();
          for (Iterator i = m.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry) i.next();
            StatementDetail d = (StatementDetail) e.getValue();
            if (!d.type.equals("select")) {
              i.remove();
            }
            
          }
          resultMaps = m;
        }
        return resultMaps;
    }

    private void reload()
    {
        r = null;
        resultMaps = null;
    }

    public JComponent getComponent()
    {
        if(panel == null)
        {
            panel = new JPanel(new BorderLayout());
            panel.add(getLeft(), "West");
            panel.add(getCenter(), "Center");
        }
        return panel;
    }

    private JPanel getLeft()
    {
        if(leftPane == null)
        {
            leftPane = new JPanel();
            leftPane.setLayout(new BoxLayout(leftPane, 1));
            leftPane.add(getCmbView());
            leftPane.add(getButtonGroup());
        }
        return leftPane;
    }

    private JSplitPane getCenter()
    {
        if(centerPane == null)
        {
            centerPane = new JSplitPane(0);
            JSplitPane top = new JSplitPane(0);
            top.setTopComponent(new JScrollPane(getParameterList()));
            top.setBottomComponent(getTxtView());
            top.setResizeWeight(0.20000000000000001D);
            centerPane.setTopComponent(top);
            JSplitPane bottom = new JSplitPane(0);
            bottom.setTopComponent(new JScrollPane(getParameterTable()));
            bottom.setBottomComponent(getErrView());
            bottom.setResizeWeight(0.29999999999999999D);
            centerPane.setResizeWeight(0.5D);
            centerPane.setBottomComponent(bottom);
        }
        return centerPane;
    }

    private JTable getParameterTable()
    {
        if(paramTable == null)
        {
            paramTable = new JTable();
            paramTable.setEnabled(false);
        }
        return paramTable;
    }

    private JTable getParameterList()
    {
        if(paramList == null)
        {
            paramList = new JTable();
            paramList.setEnabled(false);
        }
        return paramList;
    }

    private JPanel getButtonGroup()
    {
        if(ButtonGroup == null)
        {
            ButtonGroup = new JPanel();
            ButtonGroup.setLayout(new BoxLayout(ButtonGroup, 0));
            ButtonGroup.add(getButRef());
            ButtonGroup.add(getButExe());
            ButtonGroup.add(getButLoop());
        }
        return ButtonGroup;
    }

    private JList getCmbName()
    {
        if(cmbName == null)
        {
            cmbName = new JList();
            Object tempMap[] = getResultMaps().keySet().toArray();
            Arrays.sort(tempMap);
            cmbName.setListData(tempMap);
            cmbName.addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent e)
                {
                    if(cmbName.getSelectedValue() != null)
                    {
                        String statement = cmbName.getSelectedValue().toString();
                        com.profitera.ibatis.StatementDetail sd = (com.profitera.ibatis.StatementDetail)getResultMaps().get(statement);
                        getTxtDetail().setText(sd.toString());
                        getTxtDetail().setCaretPosition(0);
                        getButExe().setEnabled(true);
                        getParameterTable().setModel(getStatementTableModel(statement));
                        getParameterTable().setEnabled(true);
                        if(sd.parameterMap != null)
                            getParameterList().setModel(getListTableModel(statement));
                        else
                            getParameterList().setModel(new DefaultTableModel(0, 0));
                    } else
                    {
                        getButExe().setEnabled(false);
                        getParameterTable().setModel(new DefaultTableModel(0, 0));
                        getParameterList().setModel(new DefaultTableModel(0, 0));
                        getParameterTable().setEnabled(false);
                        getTxtDetail().setText("");
                        getTxtError().setText("");
                    }
                }

            }
);
        }
        return cmbName;
    }

    private JScrollPane getCmbView()
    {
        if(cmbView == null)
        {
            cmbView = new JScrollPane(getCmbName());
            cmbView.setVerticalScrollBarPolicy(20);
            cmbView.setHorizontalScrollBarPolicy(30);
            cmbView.setColumnHeaderView(new JLabel("Select A Statment"));
        }
        return cmbView;
    }

    private JTextArea getTxtDetail()
    {
        if(txtDetail == null)
        {
            txtDetail = new JTextArea(15, 50);
            txtDetail.setEditable(false);
            txtDetail.setLineWrap(true);
            txtDetail.setWrapStyleWord(true);
            if(getCmbName().getSelectedValue() != null)
                txtDetail.setText(getResultMaps().get(getCmbName().getSelectedValue()).toString());
        }
        return txtDetail;
    }

    private JScrollPane getTxtView()
    {
        if(txtView == null)
        {
            txtView = new JScrollPane(getTxtDetail());
            txtView.setVerticalScrollBarPolicy(20);
            txtView.setHorizontalScrollBarPolicy(30);
            txtView.setColumnHeaderView(new JLabel("Selected Statment"));
        }
        return txtView;
    }

    private JButton getButRef()
    {
        if(butRef == null)
        {
            butRef = new JButton("Refresh");
            butRef.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e)
                {
                    Thread t = new Thread(new RefreshRun());
                    t.start();
                }

            }
);
        }
        return butRef;
    }

    private JButton getButExe()
    {
        if(butExe == null)
        {
            butExe = new JButton("Execute");
            butExe.setEnabled(false);
            butExe.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e)
                {
                    if(getCmbName().getSelectedValue() != null)
                    {
                        getTxtError().setText("");
                        Thread t = new Thread(new RunQuery(getCmbName().getSelectedValues()));
                        t.start();
                    }
                }

            }
);
        }
        return butExe;
    }

    private JButton getButLoop()
    {
        if(butLoop == null)
        {
            butLoop = new JButton("Execute All");
            butLoop.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e)
                {
                    getTxtError().setText("");
                    Object tempMap[] = getResultMaps().keySet().toArray();
                    Arrays.sort(tempMap);
                    Thread t = new Thread(new RunQuery(tempMap));
                    t.start();
                }

            }
);
        }
        return butLoop;
    }

    private JTextArea getTxtError()
    {
        if(txtError == null)
        {
            txtError = new JTextArea(15, 50);
            txtError.setEditable(false);
        }
        return txtError;
    }

    private JScrollPane getErrView()
    {
        if(errView == null)
        {
            errView = new JScrollPane(getTxtError());
            errView.setVerticalScrollBarPolicy(20);
            errView.setHorizontalScrollBarPolicy(30);
            errView.setColumnHeaderView(new JLabel("Message"));
        }
        return errView;
    }

    private void printStackTrace(Exception e)
    {
        StringWriter st = new StringWriter();
        e.printStackTrace(new PrintWriter(st));
        getTxtError().append(st.toString());
        getTxtError().setCaretPosition(getTxtError().getText().length());
    }

    private SqlMapReadOnlyProvider getSqlMapReadOnlyProvider()
    {
        if(p == null)
            p = new SqlMapReadOnlyProvider();
        return p;
    }

    private void reloadSqlMapReadOnlyProvider()
    {
        if(p != null)
            p.reload();
    }

    private void myRun(String s)
        throws SQLException, ParseException
    {
        com.profitera.ibatis.StatementDetail sd = (com.profitera.ibatis.StatementDetail)getResultMaps().get(s);
        getTxtError().append("\nQuery Executing: " + s + "\n");
        Object arg = null;
        DefaultTableModel argModel = (DefaultTableModel)getParameterTable().getModel();
        if(sd.parameterType.equals(java.util.HashMap.class)){
          throw new SQLException("Use of 'hmap' or '" + HashMap.class.getName() + "' in parameter maps is not recommended, use 'map' instead");
        }
        if(sd.parameterType.equals(java.util.Map.class))
        {
            arg = new HashMap();
            Map argMap = (Map)arg;
            for(int i = 0; i < argModel.getRowCount(); i++)
            {
                String v = (String)getParameterTable().getModel().getValueAt(i, 0);
                if(v != null && !v.equals(""))
                {
                    Object value = extractValue((java.util.List)argModel.getDataVector().get(i));
                    argMap.put(v, value);
                }
            }

        } else
        {
            String v = (String)getParameterTable().getModel().getValueAt(0, 0);
            if(v != null && !v.equals(""))
                arg = extractValue((java.util.List)argModel.getDataVector().get(0));
        }
        getTxtError().append("Query '" + s + "' arg: " + arg + "\n");
        getSqlMapReadOnlyProvider().query(IReadOnlyDataProvider.LIST_RESULTS, s, arg);
        
        
        getTxtError().append("Query '" + s + "' Executed\n");
    }

    private Object extractValue(java.util.List row)
        throws ParseException
    {
        String type = (String)row.get(1);
        String value = (String)row.get(2);
        Object realValue = getItemValue(type, value);
        return realValue;
    }

    private Object getItemValue(String type, String value)
        throws ParseException
    {
        Object realValue = value;
        if(type != null && type.startsWith("l:"))
        {
            type = type.substring(2);
            realValue = new ArrayList();
            java.util.List valueList = (java.util.List)realValue;
            String values[] = value.split("[;]");
            for(int i = 0; i < values.length; i++)
                valueList.add(getItemValue(type, values[i]));

        } else
        if(type != null)
            if(type.equalsIgnoreCase("long"))
                realValue = new Long(value);
            else
            if(type.equalsIgnoreCase("int"))
                realValue = new Integer(value);
            else
            if(type.equalsIgnoreCase("date"))
                realValue = DATE_FORMAT.parse(value);
            else
            if(type.equalsIgnoreCase("double"))
                realValue = new Double(value);
        return realValue;
    }

    private DefaultTableModel getStatementTableModel(final String statement)
    {
        final DefaultTableModel m = new DefaultTableModel(COLUMN_NAMES, 10);
        m.addTableModelListener(new TableModelListener() {

            public void tableChanged(TableModelEvent e)
            {
                m.removeTableModelListener(this);
                File f = getStatementFile(statement);
                try
                {
                    ObjectOutputStream s = new ObjectOutputStream(new FileOutputStream(f));
                    s.writeObject(m.getDataVector());
                    s.close();
                }
                catch(FileNotFoundException e1)
                {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(getComponent(), e1.getMessage(), "Save Error", 0);
                }
                catch(IOException e1)
                {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(getComponent(), e1.getMessage(), "Save Error", 0);
                }
                m.addTableModelListener(this);
            }

        }
);
        File statementFile = getStatementFile(statement);
        if(statementFile.exists())
            try
            {
                ObjectInputStream s = new ObjectInputStream(new FileInputStream(statementFile));
                Vector v = (Vector)s.readObject();
                Vector headers = new Vector();
                for(int i = 0; i < COLUMN_NAMES.length; i++)
                    headers.add(COLUMN_NAMES[i]);

                m.setDataVector(v, headers);
                s.close();
            }
            catch(FileNotFoundException e1)
            {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(getComponent(), e1.getMessage(), "Save Error", 0);
            }
            catch(IOException e1)
            {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(getComponent(), e1.getMessage(), "Save Error", 0);
            }
            catch(ClassNotFoundException e1)
            {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(getComponent(), e1.getMessage(), "Save Error", 0);
            }
        return m;
    }

    private DefaultTableModel getListTableModel(String statement)
    {
        com.profitera.ibatis.StatementDetail sd = (com.profitera.ibatis.StatementDetail)getResultMaps().get(statement);
        com.profitera.ibatis.SQLMapResource.ParameterMap pm = sd.parameterMap;
        DefaultTableModel m = new DefaultTableModel(LIST_COLUMN_NAMES, pm.properties.length);
        for(int i = 0; i < pm.properties.length; i++)
        {
            m.setValueAt(pm.properties[i], i, 0);
            m.setValueAt(pm.javaTypes[i], i, 1);
            m.setValueAt(pm.jdbcTypes[i], i, 2);
        }

        return m;
    }

    private File getStatementFile(String statement)
    {
        return new File(argumentsPath.getAbsolutePath() + File.separator + statement);
    }

    private static final String COLUMN_NAMES[] = {
        "NAME", "TYPE", "VALUE"
    };
    private static final String LIST_COLUMN_NAMES[] = {
        "NAME", "JAVA TYPE", "JDBC TYPE"
    };
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    private JPanel panel;
    private JPanel ButtonGroup;
    private JPanel leftPane;
    private JSplitPane centerPane;
    private JButton butRef;
    private JButton butExe;
    private JButton butLoop;
    private JTextArea txtDetail;
    private JTextArea txtError;
    private JScrollPane txtView;
    private JScrollPane errView;
    private JScrollPane cmbView;
    private JList cmbName;
    private Map resultMaps;
    private SQLMapResource r;
    private SqlMapReadOnlyProvider p;
    private JTable paramTable;
    private JTable paramList;
    private final File argumentsPath;

















}


/*
  DECOMPILATION REPORT

  Decompiled from: C:\Documents and Settings\Jamison Masse\workspaces\bncms\ptribatis\lib\SQLMapTestHarness.jar
  Total time: 4000 ms
  Jad reported messages/errors:
  Exit status: 0
  Caught exceptions:
*/