from django.conf.urls import patterns, include, url

urlpatterns = patterns('',
    url(r'^home$', 'PlotDistribution.views.home'),
    url(r'^Plot', 'PlotDistribution.views.Plot'),
    url(r'^Update', 'PlotDistribution.views.Update'),
    url(r'^ClickPlot', 'PlotDistribution.views.ClickPlot'),
    url(r'^Egonet', 'PlotDistribution.views.Egonet'),
    url(r'^GetEgonet','PlotDistribution.views.GetEgonet'),
    url(r'^Heatmap','PlotDistribution.views.Heatmap'),
    url(r'^DBPlot','PlotDistribution.views.DBPlot'),
    url(r'^GetPlotData','PlotDistribution.views.GetPlotData'),
    url(r'^MultiPlots','PlotDistribution.views.MultiPlots'),
)
