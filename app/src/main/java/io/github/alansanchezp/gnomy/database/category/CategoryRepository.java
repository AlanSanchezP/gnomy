package io.github.alansanchezp.gnomy.database.category;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.database.GnomyDatabase;

public class CategoryRepository {
    private CategoryDAO categoryDAO;
    LiveData<List<Category>> allCategories;

    public CategoryRepository(Context context) {
        GnomyDatabase db;
        db = GnomyDatabase.getInstance(context, "");
        categoryDAO = db.categoryDAO();
        allCategories = categoryDAO.getAll();
    }

    public LiveData<List<Category>> getAll() {
        return allCategories;
    }

    public LiveData<Category> find(int categoryId) {
        return categoryDAO.find(categoryId);
    }

    public void insert(Category category) {
        InsertAsyncTask accountTask = new InsertAsyncTask(categoryDAO);
        accountTask.execute(category);
    }

    public void delete(Category category) {
        DeleteAsyncTask task = new DeleteAsyncTask(categoryDAO);
        task.execute(category);
    }

    public void update(Category category) {
        UpdateAsyncTask task = new UpdateAsyncTask(categoryDAO);
        task.execute(category);
    }

    // AsyncTask classes

    private static class InsertAsyncTask extends AsyncTask<Category, Void, Void> {

        private CategoryDAO asyncTaskDao;

        InsertAsyncTask(CategoryDAO dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Category... params) {
            asyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Category, Void, Void> {

        private CategoryDAO asyncTaskDao;

        DeleteAsyncTask(CategoryDAO dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Category... params) {
            asyncTaskDao.delete(params);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Category, Void, Void> {

        private CategoryDAO asyncTaskDao;

        UpdateAsyncTask(CategoryDAO dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Category... accounts) {
            asyncTaskDao.update(accounts[0]);
            return null;
        }
    }
}
