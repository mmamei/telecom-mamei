<#macro profiles p l><#if p == 'Transit'><#if l == 'true'>people in transit<#else>People in transit</#if><#else><#if l == 'true'>${p?lower_case}s<#else>${p}s</#if></#if></#macro>
<#if inout>
In Figure \ref{fig:heat-${city}-${month}-inout} it is possible to see a heat map showing the locations in which different profiles enter and exit the region around ${city}.
<#if paragraph==1>This is computed by considering the first and the last CDR produced by each user. For the sake of visualization we removed all those first and last CDR generated within the city boundaries.</#if>
<#else>
In Figure \ref{fig:heat-${city}-${month}-inout} we present a heat map showing the locations frequented by different users' profiles.
</#if>
<#if (random < 50)>
Looking at the figure, it is possible to see that
<#else>
The figure shows that
</#if>
<#if profiles_no_description?has_content>
<#list profiles_no_description as profile>
<@profiles p="${profile}" l="true"/><#if profile_has_next>, </#if>
</#list> are not strongly clustered around any areas<#if !inout>, although they concentrate in the city center</#if>. 
</#if>
<#if profile_descriptions?has_content>
On the other hand, 
<#list profile_descriptions?keys as profile>
	<#if profile_index == 0><@profiles p="${profile}" l="true"/><#else><@profiles p="${profile}" l="false"/></#if> tend to cluster around 
    <#assign places = profile_descriptions[profile]>
    <#list places as place>
        {\it ${place}} <#if place_has_next> and </#if>
    </#list> area<#if (places?size > 1)>s</#if>.
     <#if inout>
    	<#if (places?size > 1)>
    	This is expected in that they are commuting places.
    	<#else>
    	This is expected as this is a transit hub in the area.
    	</#if>
    <#elseif profile == 'Tourist' >
    	<#if (places?size > 1)>
    	This is rather expected as these places are main tourist spots in the area.
    	<#else>
    	This is rather expected as this is one of the main tourist spot in the area.
    	</#if>
    <#elseif profile == 'Excursionist'>
    	<#if (places?size > 1)>
    	In fact these places are notable tourist attractions.
    	<#else>
    	In fact this place is an important tourist attraction.
    	</#if>
    </#if>
</#list>
</#if>



\begin{figure}
\begin{center}
\centerline{
\includegraphics[width=0.5\columnwidth]{img/heatmap/file-pls-${region}-${city}-cellXHour-${month}/file-pls-${region}-${city}-cellXHour-${month}-Resident<#if inout>-inout</#if>.pdf} 
\includegraphics[width=0.5\columnwidth]{img/heatmap/file-pls-${region}-${city}-cellXHour-${month}/file-pls-${region}-${city}-cellXHour-${month}-Commuter<#if inout>-inout</#if>.pdf}}
\vspace{0.1cm}
\centerline{
\includegraphics[width=0.5\columnwidth]{img/heatmap/file-pls-${region}-${city}-cellXHour-${month}/file-pls-${region}-${city}-cellXHour-${month}-Transit<#if inout>-inout</#if>.pdf} 
\includegraphics[width=0.5\columnwidth]{img/heatmap/file-pls-${region}-${city}-cellXHour-${month}/file-pls-${region}-${city}-cellXHour-${month}-Excursionist<#if inout>-inout</#if>.pdf}}
\vspace{0.1cm}
\includegraphics[width=0.5\columnwidth]{img/heatmap/file-pls-${region}-${city}-cellXHour-${month}/file-pls-${region}-${city}-cellXHour-${month}-Tourist<#if inout>-inout</#if>.pdf}
\end{center}
\caption{{\bf ${city} in ${month}<#if inout> (in/out)</#if>}. From left to right, top to bottom, distribution of: residents, commuters, people in transit, excursionists, tourists.}
\label{fig:heat-${city}-${month}<#if inout>-inout</#if>}
\end{figure}