package gui.paneLinear;

import gui.paneRestriction.SelectedRestrictionEnzyme;
import gui.resource.ImgResource;
import seq.AnnotatedSequence;
import seq.SequenceRange;

import com.trolltech.qt.QSignalEmitter;
import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSlider;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

/**
 * 
 * Pane: Linear sequence view
 * 
 * @author Johan Henriksson
 *
 */
public class PaneLinearSequence extends QWidget
	{
	private QMenu menuSettings=new QMenu();

	private QSlider sliderZoom=new QSlider(Orientation.Horizontal);
	private QPushButton bSettings=new QPushButton(new QIcon(ImgResource.imgSettings), "");

	private ViewLinearSequence view=new ViewLinearSequence();
	
	public QSignalEmitter.Signal1<SequenceRange> signalSelectionChanged=new Signal1<SequenceRange>();
	public QSignalEmitter.Signal0 signalUpdated=new Signal0();

	
	/**
	 * Constructor
	 */
	public PaneLinearSequence()
		{
		QVBoxLayout lay=new QVBoxLayout();
		setLayout(lay);

		

		QHBoxLayout laycirc=new QHBoxLayout();
		
		sliderZoom.setRange(0, 5000);
		sliderZoom.setValue(0);
		sliderZoom.valueChanged.connect(this,"updateview()");
		
		laycirc.addWidget(ImgResource.label(ImgResource.search));
		laycirc.addWidget(sliderZoom);
		laycirc.addWidget(bSettings);

		menuSettings.addMenu(view.settings);
		
		bSettings.setMenu(menuSettings);

		lay.addLayout(laycirc);
		lay.addWidget(view);

		view.settings.signalSettingsChanged.connect(this,"updateview()");  //train wreck
		view.signalUpdated.connect(signalUpdated,"emit()");
		view.signalSelectionChanged.connect(this,"onSelectionChanged(SequenceRange)");
		
		updateview();
		
		
		}
	public void onSelectionChanged(SequenceRange r)
		{
		signalSelectionChanged.emit(r);
		}
	
	
	public void updateview()
		{
		view.charWidth=8+(sliderZoom.value()/(double)sliderZoom.maximum())*10;
		setSequence(view.seq); //cruel
		}

	public void setSequence(AnnotatedSequence seq)
		{
		view.setSequence(seq);
		}

	public void setSelection(SequenceRange range)
		{
		view.setSelection(range);
		}


	public SequenceRange getSelection()
		{
		return view.getSelection();
		}
	
	public void setRestrictionEnzyme(SelectedRestrictionEnzyme enz)
		{
		view.selectedEnz=enz;
		updateview();
		}

	}
