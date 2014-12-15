# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('PlotDistribution', '0001_initial'),
    ]

    operations = [
        migrations.AlterField(
            model_name='edge',
            name='fromNode',
            field=models.IntegerField(default=0),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='edge',
            name='toNode',
            field=models.IntegerField(default=0),
            preserve_default=True,
        ),
    ]
