/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;


/**
 * Genericall represents a service.
 */
public interface RootService {

	/**
	 * Open the factory.
	 *
	 * @throws Exception
	 *             the exception
	 */
	public void openFactory() throws Exception;

	/**
	 * Close the factory.
	 *
	 * @throws Exception
	 *             the exception
	 */
	public void closeFactory() throws Exception;

	/**
	 * Gets the transaction per operation.
	 *
	 * @return the transaction per operation
	 * @throws Exception the exception
	 */
	public boolean getTransactionPerOperation() throws Exception;

	/**
	 * Sets the transaction per operation.
	 *
	 * @param transactionPerOperation            the new transaction per operation
	 * @throws Exception the exception
	 */
	public void setTransactionPerOperation(boolean transactionPerOperation)
			throws Exception;

    /**
     * Commit.
     *
     * @throws Exception the exception
     */
    public void commit() throws Exception;

    /**
     * Rollback.
     *
     * @throws Exception the exception
     */
    public void rollback() throws Exception;

	/**
	 * Begin transaction.
	 *
	 * @throws Exception the exception
	 */
	public void beginTransaction() throws Exception;

	/**
	 * Closes the manager.
	 *
	 * @throws Exception the exception
	 */
	public void close() throws Exception;

    /**
     * Clears the manager.
     *
     * @throws Exception the exception
     */
    public void clear() throws Exception;
}