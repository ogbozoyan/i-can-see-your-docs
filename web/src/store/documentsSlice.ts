import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { getDocuments } from "../api/document";

export const getDocsFromServer = createAsyncThunk(
  "documents/get",
  async () => await getDocuments()
);

// TODO: дописать стор, все запросы делать через стор


const initialState = {
  isLoading: false,
  docs: [],
};

export const documentsSlice = createSlice({
  name: "documents",
  initialState,
  reducers: {
    setLoading: (state) => {
      state.isLoading = !state.isLoading;
    },
  },
  selectors: {
    getDocs: (state) => state.docs,
    getIsLoading: (state) => state.isLoading,
  },

  extraReducers: (builder) => {
    builder
      .addCase(getDocsFromServer.pending, (state) => {
        state.isLoading = true;
      })
      .addCase(getDocsFromServer.fulfilled, (state, action) => {
        state.docs = action.payload;
        state.isLoading = false;
      })
      .addCase(getDocsFromServer.rejected, (state) => {
        state.isLoading = false;
      });
  },
});

export const documentsReducer = documentsSlice.reducer;

export const { getDocs, getIsLoading } = documentsSlice.selectors;

export const { setLoading } = documentsSlice.actions;
