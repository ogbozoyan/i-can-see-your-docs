import { combineReducers, configureStore } from "@reduxjs/toolkit";
import { documentsReducer, documentsSlice } from "./documentsSlice";
import {
  useDispatch as dispatchHook,
  useSelector as selectorHook
} from 'react-redux';

export const rootReducer = combineReducers({
  documents: documentsReducer,
});

export const store = configureStore({
  reducer: rootReducer,
});

export const useDispatch = () => dispatchHook();
export const useSelector = selectorHook;
