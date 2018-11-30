import React from 'react';
import style from './path-form.scss';
import Fab from '@material-ui/core/Fab/Fab';
import AddIcon from '@material-ui/icons/Add';
import HighlightOff from '@material-ui/icons/HighlightOff';
import Zoom from '@material-ui/core/Zoom/Zoom';
import TextField from '@material-ui/core/TextField/TextField';


const NewButton = ({ onPress }) => (
  <Fab color="primary" aria-label="Add" onMouseUp={onPress}>
    <AddIcon/>
  </Fab>
);

const CloseButton = ({ onPress }) => (
  <Fab color="secondary" aria-label="Close" onMouseUp={onPress}>
    <HighlightOff/>
  </Fab>
);

const Form = ({ enterPath, onPathEntered }) => !enterPath ? null :
  (<div className={style.form}>
    <Zoom in={enterPath}>
      <TextField
        id="outlined-with-placeholder"
        label="z-path"
        placeholder="Enter path to z-node"
        margin="normal"
        variant="outlined"
        className={style.text}
        onKeyPress={onPathEntered}
      />
    </Zoom>
  </div>);

const NewPathForm = ({ enterPathFlag, onPathEntered, onToggleFlag }) => (
  <div className={style.container}>
    <div className={style.buttons}>
      {enterPathFlag ? <CloseButton onPress={onToggleFlag}/> : <NewButton onPress={onToggleFlag}/>}
    </div>
    <Form enterPath={enterPathFlag} onPathEntered={onPathEntered}/>
  </div>
);

export default NewPathForm;
