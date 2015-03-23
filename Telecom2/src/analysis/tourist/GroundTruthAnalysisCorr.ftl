
As a first analysis, we analyzed the correlation that exists between official data and data obtained by our classification. 
Of course, we do not expect to obtain the same numbers as tourists might use different network carriers or might not use the cell phone altogether. 
However, we will evaluate the correlation between the results.
Figure \ref{fig:correlation} show correlation results between official (groundtruth) data and classification results. Computed Pearson correlation is ${r}, indicating strong 
correlation between classified and groundtruth number of tourists.
On the basis of this strong correlation, it is possible to create a linear regression model to estimate the number of 
tourists on the basis of our classification. Fitting the model with least mean square errors, we obtain: 
$nTourists = ${slope} * estimated <#if (intercept > 0)>+</#if> ${intercept}$ and
a relative absolute error of ${avg_rel_abs_error}\%.



\begin{figure}
\begin{center}
\includegraphics[width=1.0\columnwidth]{img/correlation.pdf}
\end{center}
\caption{Correlation between official (groundtruth) data and classification
results. Pearson = ${r}}
\label{fig:correlation}
\end{figure}

