# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('PlotDistribution', '0006_auto_20141208_0842'),
    ]

    operations = [
        migrations.AlterField(
            model_name='inoutdegreecount',
            name='count',
            field=models.IntegerField(default=0, db_index=True),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='inoutdegreecount',
            name='inoutdegree',
            field=models.IntegerField(default=0, db_index=True),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='node',
            name='ev1',
            field=models.FloatField(default=0, db_index=True),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='node',
            name='ev2',
            field=models.FloatField(default=0, db_index=True),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='node',
            name='ev3',
            field=models.FloatField(default=0, db_index=True),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='node',
            name='inoutdegree',
            field=models.IntegerField(default=0, db_index=True),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='node',
            name='pagerank',
            field=models.FloatField(default=0, db_index=True),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='node',
            name='radius',
            field=models.FloatField(default=0, db_index=True),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='radiuscount',
            name='count',
            field=models.IntegerField(default=0, db_index=True),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='radiuscount',
            name='radius',
            field=models.FloatField(default=0, db_index=True),
            preserve_default=True,
        ),
    ]
